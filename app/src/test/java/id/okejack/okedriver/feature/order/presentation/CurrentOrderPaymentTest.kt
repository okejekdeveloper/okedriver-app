package id.okejack.okedriver.feature.order.presentation

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.data.remote.dto.OrderDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrderPaymentDto
import id.okejack.okedriver.feature.order.data.remote.dto.toDomain
import id.okejack.okedriver.feature.order.domain.model.*
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import id.okejack.okedriver.feature.order.domain.usecase.*
import id.okejack.okedriver.feature.order.presentation.current.CurrentOrderViewModel
import id.okejack.okedriver.feature.order.presentation.qris.OrderQrisViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import id.okejack.okedriver.feature.order.presentation.shopping.EditShoppingItemsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentOrderPaymentTest {
    @Before fun setMain() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun resetMain() { Dispatchers.resetMain() }

    private fun order(provider: String = "qris", paymentStatus: Int = 0, id: Long = 42) =
        OrderDto(id = id, status = DriverOrder.STATUS_DEPARTED, payment = OrderPaymentDto(provider = provider, status = paymentStatus)).toDomain()

    private fun viewModel(repo: FakeOrderRepository) = CurrentOrderViewModel(
        GetCurrentOrderUseCase(repo), UpdateCurrentOrderStatusUseCase(repo), CancelCurrentOrderUseCase(repo)
    )

    @Test fun firstRequestAndSubsequentRefreshHaveDistinctStates() = runTest {
        val repo = FakeOrderRepository(order(provider = "cash"))
        val vm = viewModel(repo)
        assertFalse(vm.state.value.hasLoaded)
        assertNull(vm.state.value.errorMessage)
        vm.loadOrder()
        assertTrue(vm.state.value.isLoading)
        advanceUntilIdle()
        assertTrue(vm.state.value.hasLoaded)
        val loaded = vm.state.value.order
        repo.current = ApiResponse.Error(500, "Koneksi terputus")
        vm.loadOrder()
        assertEquals(loaded, vm.state.value.order)
        advanceUntilIdle()
        assertEquals(loaded, vm.state.value.order)
        assertTrue(vm.state.value.hasLoaded)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun firstRequestFailureEndsLoadingWithTheActualError() = runTest {
        val repo = FakeOrderRepository(order()).apply { current = ApiResponse.Error(400, "Order sudah dibatalkan.", "order_cancelled") }
        val vm = viewModel(repo)
        vm.loadOrder(); advanceUntilIdle()
        assertTrue(vm.state.value.hasLoaded)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.order)
        assertEquals("Order sudah dibatalkan.", vm.state.value.errorMessage)
    }

    @Test fun unpaidFinishWaitsForServerPaymentAndThenFinishesOnce() = runTest {
        val repo = FakeOrderRepository(order())
        val vm = viewModel(repo)
        vm.loadOrder(); advanceUntilIdle()
        var finishedId = 0L
        vm.updateStatus { finishedId = it }; advanceUntilIdle()
        assertTrue(vm.state.value.showQris)
        assertEquals(0, repo.updateCalls)
        repo.current = ApiResponse.Success(order(paymentStatus = 2))
        vm.onQrisComplete { finishedId = it }; advanceUntilIdle()
        assertEquals(1, repo.updateCalls)
        assertEquals(42L, finishedId)
    }

    @Test fun prepaymentRefreshesWithoutFinishingTrip() = runTest {
        val repo = FakeOrderRepository(order())
        val vm = viewModel(repo)
        vm.loadOrder(); advanceUntilIdle()
        vm.openQris()
        repo.current = ApiResponse.Success(order(paymentStatus = 2))
        vm.onQrisComplete { fail("Prepayment must not finish a trip") }; advanceUntilIdle()
        assertTrue(vm.state.value.order!!.isQrisPaidInTransit)
        assertFalse(vm.state.value.showQris)
        assertEquals(0, repo.updateCalls)
    }

    @Test fun failedRefreshOrDifferentOrderNeverFinishes() = runTest {
        for (result in listOf(ApiResponse.Error(-1, "offline"), ApiResponse.Success(order(id = 99, paymentStatus = 2)), ApiResponse.Success(order()))) {
            val repo = FakeOrderRepository(order())
            val vm = viewModel(repo)
            vm.loadOrder(); advanceUntilIdle()
            vm.updateStatus { fail() }
            repo.current = result
            vm.onQrisComplete { fail("Unsafe finish after refresh") }; advanceUntilIdle()
            assertEquals(0, repo.updateCalls)
        }
    }

    @Test fun switchingToCashAllowsFinishingOnlyAfterReload() = runTest {
        val repo = FakeOrderRepository(order())
        val vm = viewModel(repo)
        vm.loadOrder(); advanceUntilIdle()
        vm.updateStatus { }
        repo.current = ApiResponse.Success(order(provider = "cash"))
        vm.onQrisComplete { }; advanceUntilIdle()
        assertEquals(1, repo.updateCalls)
    }

    @Test fun repeatedFinishTapsDoNotAdvanceTwoStatuses() = runTest {
        val repo = FakeOrderRepository(order(provider = "cash"))
        val vm = viewModel(repo)
        vm.loadOrder(); advanceUntilIdle()
        vm.updateStatus { }; vm.updateStatus { }; advanceUntilIdle()
        assertEquals(1, repo.updateCalls)
    }

    @Test fun pollingKeepsQrAndAmountWhenStatusPayloadIsPartial() = runTest {
        val repo = FakeOrderRepository(order())
        repo.qris = ApiResponse.Success(OrderQrisPayment("https://example.test/qr.png", java.math.BigDecimal("12000")))
        val vm = OrderQrisViewModel(repo)
        vm.generate(42); advanceUntilIdle()
        repo.qris = ApiResponse.Success(OrderQrisPayment(isPaid = true))
        vm.check(42); advanceUntilIdle()
        assertTrue(vm.state.value.isComplete)
        assertEquals("https://example.test/qr.png", vm.state.value.payment.qrCodeUrl)
        assertEquals(java.math.BigDecimal("12000"), vm.state.value.payment.amount)
        vm.switchToCash(42); advanceUntilIdle()
        assertEquals(0, repo.switchCalls)
    }

    @Test fun pollingErrorAndCashFailureDoNotConfirmPayment() = runTest {
        val repo = FakeOrderRepository(order())
        val vm = OrderQrisViewModel(repo)
        repo.qris = ApiResponse.Error(-1, "offline")
        vm.check(42); advanceUntilIdle()
        assertFalse(vm.state.value.isComplete)
        vm.switchToCash(42); advanceUntilIdle()
        assertFalse(vm.state.value.isComplete)
        assertEquals("cash rejected", vm.state.value.errorMessage)
    }

    @Test fun editorGuardsRepeatedSubmissionsAndAlreadyEditedOrders() = runTest {
        val repo = FakeOrderRepository(order())
        val vm = EditShoppingItemsViewModel(GetOrderDetailUseCase(repo), EditShoppingItemsUseCase(repo))
        vm.loadOrder(42); advanceUntilIdle()
        vm.addItem("Barang", "", 1, java.math.BigDecimal("5000"))
        vm.submitItems {}; vm.submitItems {}; advanceUntilIdle()
        assertEquals(1, repo.editCalls)
        repo.current = ApiResponse.Success(order().copy(shoppingEditedAt = "2026-10-28"))
        vm.loadOrder(42); advanceUntilIdle()
        vm.addItem("Tambahan", "", 1, java.math.BigDecimal.ONE)
        vm.submitItems {}; advanceUntilIdle()
        assertTrue(vm.state.value.isAlreadyEdited)
        assertTrue(vm.state.value.items.isEmpty())
        assertEquals(1, repo.editCalls)
    }
}

private class FakeOrderRepository(order: DriverOrder) : OrderRepository {
    var current: ApiResponse<DriverOrder> = ApiResponse.Success(order)
    var qris: ApiResponse<OrderQrisPayment> = ApiResponse.Success(OrderQrisPayment())
    var updateCalls = 0
    var switchCalls = 0
    var editCalls = 0
    override suspend fun getCurrentOrder() = current
    override suspend fun updateCurrentOrderStatus(): ApiResponse<DriverOrder> {
        updateCalls++
        return when (val result = current) {
            is ApiResponse.Success -> ApiResponse.Success(result.data.copy(status = DriverOrder.STATUS_ARRIVED))
            else -> result
        }
    }
    override suspend fun generateOrderQris(orderId: Long) = qris
    override suspend fun checkOrderQrisStatus(orderId: Long) = qris
    override suspend fun switchOrderToCash(orderId: Long): ApiResponse<Unit> {
        switchCalls++
        return ApiResponse.Error(400, "cash rejected")
    }
    override suspend fun getOrders(page: Int, inProgress: Boolean) = ApiResponse.Success(emptyList<DriverOrder>())
    override suspend fun getPendingOrder() = current
    override fun getPushedOrder(orderId: Long): DriverOrder? = null
    override suspend fun getOrderDetail(orderId: Long) = current
    override suspend fun cancelCurrentOrder(reasonCode: Int) = ApiResponse.Success(Unit)
    override suspend fun getChatMessages(orderId: Long) = ApiResponse.Success(emptyList<ChatMessage>())
    override suspend fun submitChatMessage(orderId: Long, content: String) = ApiResponse.Success(emptyList<ChatMessage>())
    override suspend fun editShoppingItems(orderId: Long, items: List<EditableItem>): ApiResponse<DriverOrder> {
        editCalls++
        return current
    }
    override suspend fun getRecentOrderLocations() = ApiResponse.Success(emptyList<OrderCoordinate>())
    override suspend fun generateOkeFastCode() = ApiResponse.Success("TEST")
}

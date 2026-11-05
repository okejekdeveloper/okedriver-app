package id.okejack.okedriver.feature.wallet.presentation

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.*
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import id.okejack.okedriver.feature.wallet.domain.usecase.*
import id.okejack.okedriver.feature.wallet.presentation.topup.TopUpHistoryViewModel
import id.okejack.okedriver.feature.wallet.presentation.topup.TopUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class WalletFlowParityTest {
    @Before fun setMain() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun resetMain() { Dispatchers.resetMain() }

    @Test fun topUpDoubleTapCreatesOnlyOneRequestUsingLegacyMethodId() = runTest {
        val repo = FakeWalletRepository()
        val vm = TopUpViewModel(RequestTopUpUseCase(repo))
        vm.selectAmount(100_000)
        var navigations = 0
        vm.submit { navigations++ }; vm.submit { navigations++ }; advanceUntilIdle()
        assertEquals(listOf(16 to 100_000L), repo.topUps)
        assertEquals(1, navigations)
    }

    @Test fun historyPagesAppendOnceAndFiltersRestartAtPageOne() = runTest {
        val repo = FakeWalletRepository()
        val vm = TopUpHistoryViewModel(GetTopUpHistoryUseCase(repo))
        advanceUntilIdle()
        vm.loadMore(); vm.loadMore(); advanceUntilIdle()
        assertEquals(listOf(1, 2), repo.historyCalls.map { it.page })
        assertEquals(listOf(1L, 2L), vm.state.value.deposits.map { it.id })
        vm.applyFilters("  TRX-42  ", "2026-10-01", "2026-10-28"); advanceUntilIdle()
        assertEquals(HistoryQuery(1, "2026-10-01", "2026-10-28", "TRX-42"), repo.historyCalls.last())
        assertEquals(listOf(1L), vm.state.value.deposits.map { it.id })
    }

    @Test fun convertMinimumAndDuplicateTapAreEnforcedBeforeMoneyMutation() = runTest {
        val repo = FakeWalletRepository()
        val vm = WithdrawViewModel(GetBalanceUseCase(repo), GetPendingWithdrawUseCase(repo), SubmitWithdrawUseCase(repo))
        vm.loadData(); advanceUntilIdle()
        vm.onAmountChange("99")
        vm.submit(convertToBalance = true) {}; advanceUntilIdle()
        assertTrue(repo.withdraws.isEmpty())
        vm.onAmountChange("100")
        vm.submit(convertToBalance = true) {}; vm.submit(convertToBalance = true) {}; advanceUntilIdle()
        assertEquals(listOf(BigDecimal("100") to true), repo.withdraws)
    }

    @Test fun pendingRequestBlocksNewWithdrawal() = runTest {
        val repo = FakeWalletRepository().apply { pendingCount = 1 }
        val vm = WithdrawViewModel(GetBalanceUseCase(repo), GetPendingWithdrawUseCase(repo), SubmitWithdrawUseCase(repo))
        vm.loadData(); advanceUntilIdle()
        vm.onAmountChange("20000")
        vm.submit {}; advanceUntilIdle()
        assertTrue(repo.withdraws.isEmpty())
        assertNotNull(vm.state.value.errorMessage)
    }
}

private data class HistoryQuery(val page: Int, val from: String, val to: String, val search: String)
private class FakeWalletRepository : WalletRepository {
    val topUps = mutableListOf<Pair<Int, Long>>()
    val historyCalls = mutableListOf<HistoryQuery>()
    val withdraws = mutableListOf<Pair<BigDecimal, Boolean>>()
    var pendingCount = 0L
    override suspend fun getBalance() = ApiResponse.Success(WalletBalance(BigDecimal("50000"), BigDecimal("50000")))
    override suspend fun getPendingWithdrawCount() = ApiResponse.Success(pendingCount)
    override suspend fun submitWithdraw(amount: BigDecimal, convertToBalance: Boolean): ApiResponse<Unit> {
        withdraws += amount to convertToBalance
        return ApiResponse.Success(Unit)
    }
    override suspend fun requestTopUp(paymentMethod: Int, amount: Long): ApiResponse<TopUpDeposit> {
        topUps += paymentMethod to amount
        return ApiResponse.Success(TopUpDeposit(id = 42))
    }
    override suspend fun getTopUpHistory(page: Int, dateFrom: String, dateTo: String, search: String): ApiResponse<List<TopUpDeposit>> {
        historyCalls += HistoryQuery(page, dateFrom, dateTo, search)
        return ApiResponse.Success(if (page <= 2) listOf(TopUpDeposit(page.toLong())) else emptyList())
    }
    override suspend fun getTransactions(page: Int, withdrawableOnly: Boolean) = ApiResponse.Success(emptyList<Transaction>())
    override suspend fun getPaymentMethods() = ApiResponse.Success(TopUpCatalog.methods)
    override suspend fun getTopUpDetail(depositId: Long) = ApiResponse.Success(TopUpDeposit(depositId))
    override suspend fun createTopUpPayment(depositId: Long): ApiResponse<VirtualAccount> = ApiResponse.Error(400, "unused")
    override suspend fun getBonusBoxes() = ApiResponse.Success(emptyList<BonusBox>())
    override suspend fun openBonusBox(id: Int): ApiResponse<BonusBox> = ApiResponse.Error(400, "unused")
}

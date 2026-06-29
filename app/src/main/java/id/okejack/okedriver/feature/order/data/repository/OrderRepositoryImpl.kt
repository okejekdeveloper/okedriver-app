package id.okejack.okedriver.feature.order.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.order.data.remote.OrderApiService
import id.okejack.okedriver.feature.order.data.remote.dto.ChatMessagesResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.EditShoppingItemRequestDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrderResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrdersListResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.ShoppingItemDto
import id.okejack.okedriver.feature.order.data.remote.dto.toDomain
import id.okejack.okedriver.feature.order.data.remote.dto.toMessageList
import id.okejack.okedriver.feature.order.data.remote.dto.toOrderList
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApiService,
    private val sessionDataStore: SessionDataStore
) : OrderRepository {

    override suspend fun getOrders(page: Int): ApiResponse<List<DriverOrder>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.orders(apiToken, page) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) {
                    ApiResponse.Success(dto.toOrderList())
                } else {
                    ApiResponse.Error(400, dto.errorMessage)
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getPendingOrder(): ApiResponse<DriverOrder> =
        callWithSession { apiToken -> api.pendingOrder(apiToken) }

    override suspend fun getCurrentOrder(): ApiResponse<DriverOrder> =
        callWithSession { apiToken -> api.currentOrder(apiToken) }

    override suspend fun getOrderDetail(orderId: Long): ApiResponse<DriverOrder> =
        callWithSession { apiToken -> api.viewOrder(orderId, apiToken) }

    override suspend fun updateCurrentOrderStatus(): ApiResponse<DriverOrder> =
        callWithSession { apiToken -> api.updateCurrentOrderStatus(apiToken) }

    override suspend fun cancelCurrentOrder(reasonCode: Int): ApiResponse<Unit> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        return when (val result = safeApiCall { api.cancelCurrentOrder(reasonCode, apiToken) }) {
            is ApiResponse.Success -> {
                if (result.data.isSuccessful) {
                    ApiResponse.Success(Unit)
                } else {
                    ApiResponse.Error(400, result.data.errorMessage)
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun editShoppingItems(
        orderId: Long,
        items: List<EditableItem>
    ): ApiResponse<DriverOrder> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        val body = EditShoppingItemRequestDto(
            items = items.map {
                ShoppingItemDto(
                    id = it.id,
                    name = it.name,
                    detail = it.detail,
                    qty = it.quantity,
                    price = it.price,
                    type = it.type
                )
            }
        )

        return when (val result = safeApiCall { api.editShoppingItems(orderId, body, apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (!dto.isSuccessful) return ApiResponse.Error(400, dto.errorMessage)
                val order = dto.data?.order?.toDomain()
                if (order != null) ApiResponse.Success(order)
                else ApiResponse.Error(404, "Data order tidak ditemukan setelah update")
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getChatMessages(orderId: Long): ApiResponse<List<ChatMessage>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.chatMessages(orderId, apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(dto.toMessageList())
                else ApiResponse.Error(400, dto.errorMessage)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun submitChatMessage(orderId: Long, content: String): ApiResponse<List<ChatMessage>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.submitChatMessage(orderId, content, apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(dto.toMessageList())
                else ApiResponse.Error(400, dto.errorMessage)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun callWithSession(
        request: suspend (String) -> OrderResponseDto
    ): ApiResponse<DriverOrder> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        return when (val result = safeApiCall { request(apiToken) }) {
            is ApiResponse.Success -> result.data.toApiResponse()
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private fun OrderResponseDto.toApiResponse(): ApiResponse<DriverOrder> {
        if (!isSuccessful) {
            return ApiResponse.Error(400, errorMessage)
        }

        val order = data?.order?.toDomain()
        return if (order != null && order.id > 0) {
            ApiResponse.Success(order)
        } else {
            ApiResponse.Error(404, "Order tidak ditemukan")
        }
    }
}

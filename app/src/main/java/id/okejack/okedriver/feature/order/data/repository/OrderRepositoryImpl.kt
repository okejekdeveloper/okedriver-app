package id.okejack.okedriver.feature.order.data.repository

import android.util.Log
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.core.service.OrderPushCache
import id.okejack.okedriver.feature.order.data.remote.dto.PushedOrderParser
import id.okejack.okedriver.feature.order.data.remote.OrderApiService
import id.okejack.okedriver.feature.order.data.remote.dto.ChatMessagesResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.EditShoppingItemRequestDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrderResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrdersListResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.ShoppingItemDto
import id.okejack.okedriver.feature.order.data.remote.dto.toSaveResult
import id.okejack.okedriver.feature.order.data.remote.dto.toDomain
import id.okejack.okedriver.feature.order.data.remote.dto.toCoordinateList
import id.okejack.okedriver.feature.order.data.remote.dto.toMessageList
import id.okejack.okedriver.feature.order.data.remote.dto.toOrderList
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import id.okejack.okedriver.feature.order.domain.model.OrderCoordinate
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import id.okejack.okedriver.feature.order.data.remote.dto.ChatMessageBody
import id.okejack.okedriver.feature.order.data.remote.dto.OrderIdBody
import id.okejack.okedriver.feature.order.data.remote.dto.OrderQrisResponseDto
import id.okejack.okedriver.feature.order.domain.model.OrderQrisPayment

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApiService,
    private val sessionDataStore: SessionDataStore,
    private val orderPushCache: OrderPushCache
) : OrderRepository {

    override fun getPushedOrder(orderId: Long): DriverOrder? {
        val json = orderPushCache.get(orderId)
        if (json == null) {
            Log.d(TAG, "Pushed order $orderId unavailable: none cached (not received by this process, or older than the offer window)")
            return null
        }
        return when (val result = PushedOrderParser.parse(json, orderId)) {
            is PushedOrderParser.Result.Parsed -> {
                Log.d(TAG, "Pushed order $orderId: showing the offer from the push payload")
                result.order
            }
            is PushedOrderParser.Result.Failed -> {
                Log.d(TAG, "Pushed order $orderId unusable: ${result.reason}")
                null
            }
        }
    }

    private companion object {
        const val TAG = "OkeDriver"
    }

    override suspend fun getOrders(page: Int, inProgress: Boolean): ApiResponse<List<DriverOrder>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.orders(apiToken, page, if (inProgress) 1 else 0) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) {
                    ApiResponse.Success(dto.toOrderList())
                } else {
                    ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
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
                    ApiResponse.Error(400, result.data.errorMessage, errorCode = result.data.error)
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun editShoppingItems(
        orderId: Long,
        items: List<EditableItem>
    ): ApiResponse<Unit> {
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
            is ApiResponse.Success -> result.data.toSaveResult()
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
                else ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun submitChatMessage(orderId: Long, content: String): ApiResponse<List<ChatMessage>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.submitChatMessage(orderId, ChatMessageBody(content), apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(dto.toMessageList())
                else ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getRecentOrderLocations(): ApiResponse<List<OrderCoordinate>> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.recentOrders(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(dto.toCoordinateList())
                else ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun generateOkeFastCode(): ApiResponse<String> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.generateOkeFastCode(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (!dto.isSuccessful) {
                    ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
                } else {
                    val code = dto.data?.code.orEmpty()
                    if (code.isNotBlank()) ApiResponse.Success(code)
                    else ApiResponse.Error(404, "Kode OkeFast tidak tersedia")
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun generateOrderQris(orderId: Long): ApiResponse<OrderQrisPayment> =
        callQris(orderId, api::generateOrderQris)

    override suspend fun checkOrderQrisStatus(orderId: Long): ApiResponse<OrderQrisPayment> =
        callQris(orderId, api::checkOrderQrisStatus)

    override suspend fun switchOrderToCash(orderId: Long): ApiResponse<Unit> {
        if (orderId <= 0) return ApiResponse.Error(400, "Data order tidak valid")
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.switchOrderToCash(OrderIdBody(orderId), token) }) {
            is ApiResponse.Success -> if (result.data.isSuccessful) ApiResponse.Success(Unit)
                else ApiResponse.Error(400, result.data.errorMessage, errorCode = result.data.error)
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun callQris(
        orderId: Long,
        request: suspend (OrderIdBody, String) -> OrderQrisResponseDto
    ): ApiResponse<OrderQrisPayment> {
        if (orderId <= 0) return ApiResponse.Error(400, "Data order tidak valid")
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { request(OrderIdBody(orderId), token) }) {
            is ApiResponse.Success -> {
                val response = result.data
                if (!response.isSuccessful) ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
                else response.data?.let { ApiResponse.Success(it.toDomain()) }
                    ?: ApiResponse.Error(422, "Data pembayaran tidak tersedia")
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
            return ApiResponse.Error(400, errorMessage, errorCode = error)
        }

        val order = data?.order?.toDomain()
        return if (order != null && order.id > 0) {
            ApiResponse.Success(order)
        } else {
            ApiResponse.Error(404, "Order tidak ditemukan")
        }
    }
}

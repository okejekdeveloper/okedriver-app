package id.okejack.okedriver.feature.order.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import id.okejack.okedriver.feature.order.domain.model.OrderCoordinate
import id.okejack.okedriver.feature.order.domain.model.OrderQrisPayment

interface OrderRepository {
    suspend fun getOrders(page: Int = 1, inProgress: Boolean = false): ApiResponse<List<DriverOrder>>
    suspend fun getPendingOrder(): ApiResponse<DriverOrder>

    /** The order offered by the most recent push for [orderId], if one arrived recently enough to still be an open offer. */
    fun getPushedOrder(orderId: Long): DriverOrder?
    suspend fun getCurrentOrder(): ApiResponse<DriverOrder>
    suspend fun getOrderDetail(orderId: Long): ApiResponse<DriverOrder>
    suspend fun updateCurrentOrderStatus(): ApiResponse<DriverOrder>
    suspend fun cancelCurrentOrder(reasonCode: Int): ApiResponse<Unit>
    suspend fun getChatMessages(orderId: Long): ApiResponse<List<ChatMessage>>
    suspend fun submitChatMessage(orderId: Long, content: String): ApiResponse<List<ChatMessage>>
    suspend fun editShoppingItems(orderId: Long, items: List<EditableItem>): ApiResponse<DriverOrder>
    suspend fun getRecentOrderLocations(): ApiResponse<List<OrderCoordinate>>
    suspend fun generateOkeFastCode(): ApiResponse<String>
    suspend fun generateOrderQris(orderId: Long): ApiResponse<OrderQrisPayment>
    suspend fun checkOrderQrisStatus(orderId: Long): ApiResponse<OrderQrisPayment>
    suspend fun switchOrderToCash(orderId: Long): ApiResponse<Unit>
}

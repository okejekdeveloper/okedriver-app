package id.okejack.okedriver.feature.order.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.EditableItem

interface OrderRepository {
    suspend fun getOrders(page: Int = 1): ApiResponse<List<DriverOrder>>
    suspend fun getPendingOrder(): ApiResponse<DriverOrder>
    suspend fun getCurrentOrder(): ApiResponse<DriverOrder>
    suspend fun getOrderDetail(orderId: Long): ApiResponse<DriverOrder>
    suspend fun updateCurrentOrderStatus(): ApiResponse<DriverOrder>
    suspend fun cancelCurrentOrder(reasonCode: Int): ApiResponse<Unit>
    suspend fun getChatMessages(orderId: Long): ApiResponse<List<ChatMessage>>
    suspend fun submitChatMessage(orderId: Long, content: String): ApiResponse<List<ChatMessage>>
    suspend fun editShoppingItems(orderId: Long, items: List<EditableItem>): ApiResponse<DriverOrder>
}

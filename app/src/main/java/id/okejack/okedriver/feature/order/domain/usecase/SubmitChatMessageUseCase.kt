package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class SubmitChatMessageUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: Long, content: String): ApiResponse<List<ChatMessage>> =
        repository.submitChatMessage(orderId, content)
}

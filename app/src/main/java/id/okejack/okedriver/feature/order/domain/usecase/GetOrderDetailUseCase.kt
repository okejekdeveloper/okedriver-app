package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderDetailUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: Long) = repository.getOrderDetail(orderId)
}

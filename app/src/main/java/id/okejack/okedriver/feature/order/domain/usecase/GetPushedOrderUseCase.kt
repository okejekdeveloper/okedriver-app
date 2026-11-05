package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class GetPushedOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(orderId: Long): DriverOrder? = repository.getPushedOrder(orderId)
}

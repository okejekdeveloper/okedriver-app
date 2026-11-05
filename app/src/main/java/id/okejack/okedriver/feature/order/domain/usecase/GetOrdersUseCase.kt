package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(page: Int = 1, inProgress: Boolean = false): ApiResponse<List<DriverOrder>> =
        repository.getOrders(page, inProgress)
}

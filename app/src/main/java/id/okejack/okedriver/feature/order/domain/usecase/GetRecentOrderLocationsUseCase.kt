package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class GetRecentOrderLocationsUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke() = repository.getRecentOrderLocations()
}

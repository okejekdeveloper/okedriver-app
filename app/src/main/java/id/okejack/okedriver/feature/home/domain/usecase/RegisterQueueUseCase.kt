package id.okejack.okedriver.feature.home.domain.usecase

import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class RegisterQueueUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(latLng: String, terminal: String) =
        repository.registerQueue(latLng, terminal)
}

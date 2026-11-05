package id.okejack.okedriver.feature.home.domain.usecase

import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import javax.inject.Inject
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.domain.model.QueueLocations

class RegisterQueueUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(latLng: String, terminal: String): ApiResponse<HomeSupportInfo> =
        if (terminal in QueueLocations.all) repository.registerQueue(latLng, terminal)
        else ApiResponse.Error(400, "Lokasi antrean tidak valid")
}

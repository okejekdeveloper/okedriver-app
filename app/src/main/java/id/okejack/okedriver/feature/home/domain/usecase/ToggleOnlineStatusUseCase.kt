package id.okejack.okedriver.feature.home.domain.usecase

import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class ToggleOnlineStatusUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke() = repository.toggleOnlineStatus()
}

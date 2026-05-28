package id.okejack.okedriver.feature.home.domain.usecase

import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetDriverInfoUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke() = repository.getDriverInfo()
}

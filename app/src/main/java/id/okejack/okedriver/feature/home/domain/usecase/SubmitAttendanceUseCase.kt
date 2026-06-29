package id.okejack.okedriver.feature.home.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class SubmitAttendanceUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): ApiResponse<Unit> = repository.submitAttendance()
}

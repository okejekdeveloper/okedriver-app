package id.okejack.okedriver.feature.profile.domain.usecase

import id.okejack.okedriver.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetReferralInfoUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(month: Int, year: Int) = repository.getReferralInfo(month, year)
}

package id.okejack.okedriver.feature.profile.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import id.okejack.okedriver.feature.profile.domain.model.ProfileDriver
import id.okejack.okedriver.feature.profile.domain.model.ReferralInfo

interface ProfileRepository {
    suspend fun getProfile(): ApiResponse<ProfileDriver>
    suspend fun updateProfile(form: EditProfileForm): ApiResponse<ProfileDriver>
    suspend fun getReferralInfo(month: Int, year: Int): ApiResponse<ReferralInfo>
}

package id.okejack.okedriver.feature.profile.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.profile.data.remote.ProfileApiService
import id.okejack.okedriver.feature.profile.data.remote.dto.ProfileResponseDto
import id.okejack.okedriver.feature.profile.data.remote.dto.toDomain
import id.okejack.okedriver.feature.profile.data.remote.dto.toReferralInfo
import id.okejack.okedriver.feature.profile.data.remote.dto.toRequestDto
import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import id.okejack.okedriver.feature.profile.domain.model.ProfileDriver
import id.okejack.okedriver.feature.profile.domain.model.ReferralInfo
import id.okejack.okedriver.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApiService,
    private val sessionDataStore: SessionDataStore
) : ProfileRepository {

    override suspend fun getProfile(): ApiResponse<ProfileDriver> =
        callWithSession { token -> api.driverInfo(token) }.mapProfile()

    override suspend fun updateProfile(form: EditProfileForm): ApiResponse<ProfileDriver> {
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.editProfile(form.toRequestDto(token), token) }) {
            is ApiResponse.Success -> result.data.mapProfileResponse()
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getReferralInfo(month: Int, year: Int): ApiResponse<ReferralInfo> {
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        val options = mapOf("month" to month.toString(), "year" to year.toString())
        val profileResult = callWithSession { apiToken -> api.driverInfo(apiToken) }.mapProfile()
        val fallbackDriver = (profileResult as? ApiResponse.Success)?.data

        return when (val result = safeApiCall { api.referralDrivers(options, token) }) {
            is ApiResponse.Success -> {
                val response = result.data
                if (!response.isSuccessful) {
                    ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
                } else {
                    val referralCode = safeApiCall { api.referralCode(token) }
                    val mergedData = when (referralCode) {
                        is ApiResponse.Success -> referralCode.data.data.copy(drivers = response.data.drivers)
                        else -> response.data
                    }
                    ApiResponse.Success(mergedData.toReferralInfo(fallbackDriver))
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun callWithSession(
        request: suspend (String) -> ProfileResponseDto
    ): ApiResponse<ProfileResponseDto> {
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return safeApiCall { request(token) }
    }

    private fun ApiResponse<ProfileResponseDto>.mapProfile(): ApiResponse<ProfileDriver> =
        when (this) {
            is ApiResponse.Success -> data.mapProfileResponse()
            is ApiResponse.Error -> this
            ApiResponse.Loading -> ApiResponse.Loading
        }

    private fun ProfileResponseDto.mapProfileResponse(): ApiResponse<ProfileDriver> {
        if (!isSuccessful) return ApiResponse.Error(400, errorMessage, errorCode = error)
        val driver = data.driver.toDomain()
        return if (driver.id > 0) ApiResponse.Success(driver)
        else ApiResponse.Error(404, "Data profil tidak ditemukan")
    }
}

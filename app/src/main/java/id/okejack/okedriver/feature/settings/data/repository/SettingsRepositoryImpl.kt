package id.okejack.okedriver.feature.settings.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.settings.data.remote.SettingsFeatureApiService
import id.okejack.okedriver.feature.settings.data.remote.dto.ConnectAccountRequestDto
import id.okejack.okedriver.feature.settings.data.remote.dto.SettingsResponseDto
import id.okejack.okedriver.feature.settings.data.remote.dto.toDomain
import id.okejack.okedriver.feature.settings.data.remote.dto.toRequestDto
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.model.DriverSettings
import id.okejack.okedriver.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val api: SettingsFeatureApiService,
    private val sessionDataStore: SessionDataStore
) : SettingsRepository {

    override suspend fun getSettings(): ApiResponse<DriverSettings> =
        callWithSession { api.driverInfo(it) }.toSettings()

    override suspend fun updateDriverBid(
        preferences: DriverBidPreferences
    ): ApiResponse<DriverSettings> =
        callWithSession { api.submitDriverBid(preferences.toRequestDto(), it) }.toSettings()

    override suspend fun connectAccount(
        email: String,
        password: String
    ): ApiResponse<DriverSettings> = mutateThenReload { token ->
        api.connectAccount(ConnectAccountRequestDto(email, password), token)
    }

    override suspend fun disconnectAccount(): ApiResponse<DriverSettings> =
        mutateThenReload { api.disconnectAccount(it) }

    private suspend fun mutateThenReload(
        mutation: suspend (String) -> SettingsResponseDto
    ): ApiResponse<DriverSettings> {
        val mutationResult = callWithSession(mutation)
        when (mutationResult) {
            is ApiResponse.Error -> return mutationResult
            is ApiResponse.Success -> if (!mutationResult.data.isSuccessful) {
                return ApiResponse.Error(400, mutationResult.data.errorMessage, errorCode = mutationResult.data.error)
            }
            ApiResponse.Loading -> return ApiResponse.Loading
        }
        return getSettings()
    }

    private suspend fun callWithSession(
        request: suspend (String) -> SettingsResponseDto
    ): ApiResponse<SettingsResponseDto> {
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return safeApiCall { request(token) }
    }

    private fun ApiResponse<SettingsResponseDto>.toSettings(): ApiResponse<DriverSettings> =
        when (this) {
            is ApiResponse.Success -> {
                if (!data.isSuccessful) ApiResponse.Error(400, data.errorMessage, errorCode = data.error)
                else data.data.driver.toDomain().let { settings ->
                    if (settings.driverId > 0) ApiResponse.Success(settings)
                    else ApiResponse.Error(404, "Data pengaturan driver tidak ditemukan")
                }
            }
            is ApiResponse.Error -> this
            ApiResponse.Loading -> ApiResponse.Loading
        }
}

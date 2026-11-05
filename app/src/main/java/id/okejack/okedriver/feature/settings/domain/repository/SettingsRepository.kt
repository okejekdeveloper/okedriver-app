package id.okejack.okedriver.feature.settings.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.model.DriverSettings

interface SettingsRepository {
    suspend fun getSettings(): ApiResponse<DriverSettings>
    suspend fun updateDriverBid(preferences: DriverBidPreferences): ApiResponse<DriverSettings>
    suspend fun connectAccount(email: String, password: String): ApiResponse<DriverSettings>
    suspend fun disconnectAccount(): ApiResponse<DriverSettings>
}

package id.okejack.okedriver.feature.settings.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.model.DriverSettings
import id.okejack.okedriver.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetDriverSettingsUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke() = repository.getSettings()
}

class UpdateDriverBidUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(
        preferences: DriverBidPreferences,
        requireServiceSelection: Boolean
    ): ApiResponse<DriverSettings> {
        if (requireServiceSelection && !preferences.hasAnyService()) {
            return ApiResponse.Error(400, "Pilih minimal satu jenis layanan")
        }
        return repository.updateDriverBid(preferences)
    }
}

class ConnectAccountUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResponse<DriverSettings> {
        if (email.isBlank() || password.isBlank()) {
            return ApiResponse.Error(400, "Email dan password wajib diisi")
        }
        return repository.connectAccount(email.trim(), password)
    }
}

class DisconnectAccountUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke() = repository.disconnectAccount()
}

private fun DriverBidPreferences.hasAnyService(): Boolean =
    rideEnabled || deliveryEnabled || shoppingEnabled || foodEnabled

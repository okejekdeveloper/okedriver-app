package id.okejack.okedriver.feature.settings.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.model.DriverSettings
import id.okejack.okedriver.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUseCasesTest {
    @Test
    fun `ride driver must select at least one service`() = runBlocking {
        val repository = FakeSettingsRepository()
        val result = UpdateDriverBidUseCase(repository)(disabledServices(), true)

        assertTrue(result is ApiResponse.Error)
        assertFalse(repository.updateCalled)
    }

    @Test
    fun `non ride driver can save without service selection`() = runBlocking {
        val repository = FakeSettingsRepository()
        val result = UpdateDriverBidUseCase(repository)(disabledServices(), false)

        assertTrue(result is ApiResponse.Success)
        assertTrue(repository.updateCalled)
    }

    private fun disabledServices() = DriverBidPreferences(
        rideEnabled = false,
        deliveryEnabled = false,
        shoppingEnabled = false,
        foodEnabled = false
    )
}

private class FakeSettingsRepository : SettingsRepository {
    var updateCalled = false
    private val settings = DriverSettings(1, 0, DriverBidPreferences())

    override suspend fun getSettings() = ApiResponse.Success(settings)

    override suspend fun updateDriverBid(preferences: DriverBidPreferences): ApiResponse<DriverSettings> {
        updateCalled = true
        return ApiResponse.Success(settings.copy(bid = preferences))
    }

    override suspend fun connectAccount(email: String, password: String) = ApiResponse.Success(settings)
    override suspend fun disconnectAccount() = ApiResponse.Success(settings)
}

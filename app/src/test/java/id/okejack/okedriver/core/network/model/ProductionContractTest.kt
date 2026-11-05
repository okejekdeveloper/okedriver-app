package id.okejack.okedriver.core.network.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

class ProductionContractTest {
    @Test fun legacySuccessFlagIsRecognized() {
        val response = Gson().fromJson("""{"success":true,"data":{}}""", BaseResponse::class.java)
        assertTrue(response.isSuccessful)
    }

    @Test fun minimumVersionOverridesOptionalVersion() {
        val settings = AppInitSettingsDto(currentVersionCode = 100, minimumVersionCode = 120)
        assertTrue(settings.hasUpdate(110))
        assertTrue(settings.requiresUpdate(110))
        assertFalse(settings.requiresUpdate(120))
        assertFalse(settings.hasUpdate(120))
    }

    @Test fun currentVersionOffersOptionalUpdateOnly() {
        val settings = AppInitSettingsDto(currentVersionCode = 121, minimumVersionCode = 100)
        assertTrue(settings.hasUpdate(120))
        assertFalse(settings.requiresUpdate(120))
        assertFalse(settings.hasUpdate(121))
    }

    @Test fun acceptsLegacyAndSnakeCaseInitSettings() {
        for (key in listOf("initSettings", "init_settings")) {
            val data = Gson().fromJson("""{"$key":{"current_app_version_code":200,"min_app_version_code":100}}""", AppInitDataDto::class.java)
            assertEquals(200, data.initSettings!!.currentVersionCode)
            assertTrue(data.initSettings.requiresUpdate(99))
        }
    }

    @Test fun cancelledNetworkRequestRemainsCancelled() = runBlocking {
        val cancellation = CancellationException("screen disposed")
        try {
            safeApiCall<Unit> { throw cancellation }
            fail("Cancellation must propagate to the lifecycle owner")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }
}

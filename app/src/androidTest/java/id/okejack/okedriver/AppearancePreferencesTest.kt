package id.okejack.okedriver

import androidx.test.platform.app.InstrumentationRegistry
import id.okejack.okedriver.core.datastore.AppearancePreferences
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearancePreferencesTest {
    @Test fun choiceSurvivesNewStoreAndSessionClear() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = AppearancePreferences(context)
        val original = preferences.current
        try {
            preferences.select(DriverUiStyle.SIMPLE)
            assertEquals(DriverUiStyle.SIMPLE, AppearancePreferences(context).current)
            SessionDataStore(context).clearSession()
            assertEquals(DriverUiStyle.SIMPLE, AppearancePreferences(context).current)
            preferences.select(DriverUiStyle.DYNAMIC)
            assertEquals(DriverUiStyle.DYNAMIC, AppearancePreferences(context).current)
        } finally { preferences.select(original) }
    }
}

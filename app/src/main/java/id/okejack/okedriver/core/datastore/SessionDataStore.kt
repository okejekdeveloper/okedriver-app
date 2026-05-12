package id.okejack.okedriver.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val DRIVER_ID = intPreferencesKey("driver_id")
        val DRIVER_NAME = stringPreferencesKey("driver_name")
        val DRIVER_PHONE = stringPreferencesKey("driver_phone")
        val DRIVER_PHOTO = stringPreferencesKey("driver_photo")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val ONESIGNAL_ID = stringPreferencesKey("onesignal_id")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val driverId: Flow<Int?> = context.dataStore.data.map { it[Keys.DRIVER_ID] }
    val driverName: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_NAME] }
    val driverPhone: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_PHONE] }
    val driverPhoto: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_PHOTO] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM_TOKEN] }

    suspend fun saveSession(
        token: String,
        driverId: Int,
        driverName: String,
        driverPhone: String,
        driverPhoto: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.DRIVER_ID] = driverId
            prefs[Keys.DRIVER_NAME] = driverName
            prefs[Keys.DRIVER_PHONE] = driverPhone
            prefs[Keys.DRIVER_PHOTO] = driverPhoto
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun saveFcmToken(fcmToken: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FCM_TOKEN] = fcmToken
        }
    }

    suspend fun saveOneSignalId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONESIGNAL_ID] = id
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}

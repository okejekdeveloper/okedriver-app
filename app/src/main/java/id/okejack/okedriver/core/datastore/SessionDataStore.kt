package id.okejack.okedriver.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val DRIVER_EMAIL = stringPreferencesKey("driver_email")
        val DRIVER_PHOTO = stringPreferencesKey("driver_photo")
        val DRIVER_BALANCE = stringPreferencesKey("driver_balance")
        val DRIVER_WITHDRAWABLE_BALANCE = stringPreferencesKey("driver_withdrawable_balance")
        val DRIVER_ONLINE = booleanPreferencesKey("driver_online")
        val SESSION_UPDATED_AT = longPreferencesKey("session_updated_at")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val ONESIGNAL_ID = stringPreferencesKey("onesignal_id")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val driverId: Flow<Int?> = context.dataStore.data.map { it[Keys.DRIVER_ID] }
    val driverName: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_NAME] }
    val driverPhone: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_PHONE] }
    val driverEmail: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_EMAIL] }
    val driverPhoto: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_PHOTO] }
    val driverBalance: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVER_BALANCE] }
    val driverWithdrawableBalance: Flow<String?> =
        context.dataStore.data.map { it[Keys.DRIVER_WITHDRAWABLE_BALANCE] }
    val driverOnline: Flow<Boolean> = context.dataStore.data.map { it[Keys.DRIVER_ONLINE] ?: false }
    val sessionUpdatedAt: Flow<Long> = context.dataStore.data.map { it[Keys.SESSION_UPDATED_AT] ?: 0L }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM_TOKEN] }
    val oneSignalId: Flow<String?> = context.dataStore.data.map { it[Keys.ONESIGNAL_ID] }

    suspend fun saveSession(
        token: String,
        driverId: Int,
        driverName: String,
        driverPhone: String,
        driverEmail: String = "",
        driverPhoto: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.DRIVER_ID] = driverId
            prefs[Keys.DRIVER_NAME] = driverName
            prefs[Keys.DRIVER_PHONE] = driverPhone
            prefs[Keys.DRIVER_EMAIL] = driverEmail
            prefs[Keys.DRIVER_PHOTO] = driverPhoto
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.SESSION_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun saveDriverProfile(
        driverId: Int,
        driverName: String,
        driverPhone: String,
        driverEmail: String = "",
        driverPhoto: String = "",
        balance: String = "",
        withdrawableBalance: String = "",
        online: Boolean = false
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DRIVER_ID] = driverId
            prefs[Keys.DRIVER_NAME] = driverName
            prefs[Keys.DRIVER_PHONE] = driverPhone
            prefs[Keys.DRIVER_EMAIL] = driverEmail
            prefs[Keys.DRIVER_PHOTO] = driverPhoto
            prefs[Keys.DRIVER_BALANCE] = balance
            prefs[Keys.DRIVER_WITHDRAWABLE_BALANCE] = withdrawableBalance
            prefs[Keys.DRIVER_ONLINE] = online
            prefs[Keys.SESSION_UPDATED_AT] = System.currentTimeMillis()
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

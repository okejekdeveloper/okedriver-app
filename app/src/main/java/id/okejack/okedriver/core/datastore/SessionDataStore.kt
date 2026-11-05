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
import androidx.datastore.migrations.SharedPreferencesMigration
import com.google.gson.JsonParser

private val legacySessionKeys = setOf("OKEDRIVER_SESSION_ID", "OKEDRIVER_DRIVER_ID", "OKEDRIVER_DRIVER", "firebase_app_token")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session",
    produceMigrations = { context ->
        // EasyPrefs in the production app uses Android's default preferences.
        listOf("${context.packageName}_preferences", context.packageName).map { prefsName ->
            SharedPreferencesMigration<Preferences>(context, prefsName, keysToMigrate = legacySessionKeys) { legacy, current ->
                val tokenKey = stringPreferencesKey("token")
                val token = legacy.getString("OKEDRIVER_SESSION_ID", null).orEmpty()
                if (!current[tokenKey].isNullOrBlank() || token.isBlank()) current
                else current.toMutablePreferences().apply {
                    val driver = runCatching {
                        JsonParser.parseString(legacy.getString("OKEDRIVER_DRIVER", "{}")).asJsonObject
                    }.getOrNull()
                    fun driverString(key: String) = runCatching { driver?.get(key)?.asString }.getOrNull().orEmpty()
                    val id = driverString("id").toIntOrNull()
                        ?: runCatching { legacy.getInt("OKEDRIVER_DRIVER_ID", 0) }.getOrDefault(0)
                    this[tokenKey] = token
                    this[intPreferencesKey("driver_id")] = id
                    this[stringPreferencesKey("driver_name")] = driverString("name")
                    this[stringPreferencesKey("driver_phone")] = driverString("phone")
                    this[stringPreferencesKey("driver_email")] = driverString("email")
                    this[stringPreferencesKey("driver_photo")] = driverString("image_url").ifBlank { driverString("image") }
                    this[stringPreferencesKey("driver_balance")] = driverString("balance")
                    this[stringPreferencesKey("driver_withdrawable_balance")] = driverString("withdrawable_balance")
                    this[booleanPreferencesKey("driver_online")] = driverString("online") == "true"
                    this[booleanPreferencesKey("is_logged_in")] = true
                    this[stringPreferencesKey("fcm_token")] = legacy.getString("firebase_app_token", "").orEmpty()
                }
            }
        }
    }
)

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
        val SESSION_EXPIRED = booleanPreferencesKey("session_expired")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val ONESIGNAL_ID = stringPreferencesKey("onesignal_id")
        val PAYMENT_BIDDING_RANGE = stringPreferencesKey("payment_bidding_range")
        val DISTANCE_BIDDING_RANGE = stringPreferencesKey("distance_bidding_range")
        val LAST_APP_REPORT_AT = longPreferencesKey("last_app_report_at")
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
    val sessionExpired: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SESSION_EXPIRED] ?: false }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM_TOKEN] }
    val oneSignalId: Flow<String?> = context.dataStore.data.map { it[Keys.ONESIGNAL_ID] }
    val lastAppReportAt: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_APP_REPORT_AT] ?: 0L }
    suspend fun saveAppReportAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_APP_REPORT_AT] = timestamp }
    }
    val paymentBiddingRange: Flow<List<Int>> = context.dataStore.data.map {
        it[Keys.PAYMENT_BIDDING_RANGE].orEmpty().split(",").mapNotNull(String::toIntOrNull)
    }
    val distanceBiddingRange: Flow<List<Int>> = context.dataStore.data.map {
        it[Keys.DISTANCE_BIDDING_RANGE].orEmpty().split(",").mapNotNull(String::toIntOrNull)
    }

    suspend fun saveBiddingRanges(payment: List<Int>?, distance: List<Int>?) {
        context.dataStore.edit {
            if (!payment.isNullOrEmpty()) it[Keys.PAYMENT_BIDDING_RANGE] = payment.joinToString(",")
            if (!distance.isNullOrEmpty()) it[Keys.DISTANCE_BIDDING_RANGE] = distance.joinToString(",")
        }
    }

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
            prefs[Keys.SESSION_EXPIRED] = false
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

    suspend fun expireSession(expectedToken: String? = null) {
        context.dataStore.edit { prefs ->
            if (expectedToken != null && prefs[Keys.TOKEN] != expectedToken) return@edit
            val fcmToken = prefs[Keys.FCM_TOKEN]
            prefs.clear()
            if (!fcmToken.isNullOrBlank()) prefs[Keys.FCM_TOKEN] = fcmToken
            prefs[Keys.SESSION_EXPIRED] = true
        }
    }

    suspend fun acknowledgeSessionExpired() {
        context.dataStore.edit { it[Keys.SESSION_EXPIRED] = false }
    }
}

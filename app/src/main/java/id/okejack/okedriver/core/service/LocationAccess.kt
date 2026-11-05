package id.okejack.okedriver.core.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Consent is separate from runtime permission and the server's online status. */
object LocationAccess {
    private const val CONSENT = "location_disclosure_accepted_version"
    private const val REQUESTED = "location_tracking_requested"

    private fun prefs(context: Context): SharedPreferences {
        val prefs = context.applicationContext.getSharedPreferences("location_access", Context.MODE_PRIVATE)
        synchronized(this) {
            if (!prefs.getBoolean("legacy_migrated", false)) {
                val legacy = listOf("${context.packageName}_preferences", context.packageName)
                    .map { context.getSharedPreferences(it, Context.MODE_PRIVATE) }
                    .firstOrNull { it.contains(CONSENT) }
                val version = legacy?.getInt(CONSENT, 0) ?: 0
                prefs.edit().putInt(CONSENT, version)
                    .putBoolean(REQUESTED, version == LocationTrackingPolicy.DISCLOSURE_VERSION && legacy?.getBoolean(REQUESTED, false) == true)
                    .putBoolean("legacy_migrated", true).apply()
            }
        }
        return prefs
    }

    fun hasConsent(context: Context): Boolean = acceptedVersion(context) == LocationTrackingPolicy.DISCLOSURE_VERSION
    fun acceptedVersion(context: Context): Int = prefs(context).getInt(CONSENT, 0)
    fun trackingRequested(context: Context): Boolean = prefs(context).getBoolean(REQUESTED, false)
    fun canAccess(context: Context): Boolean = LocationTrackingPolicy.canAccess(acceptedVersion(context), LocationHelper.hasForegroundLocationPermission(context))

    fun consent(context: Context) = callbackFlow {
        val prefs = prefs(context)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(hasConsent(context)) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(hasConsent(context))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun grant(context: Context) {
        prefs(context).edit().putInt(CONSENT, LocationTrackingPolicy.DISCLOSURE_VERSION).apply()
    }

    fun setTrackingRequested(context: Context, requested: Boolean) {
        prefs(context).edit().putBoolean(REQUESTED, requested && hasConsent(context)).apply()
    }

    fun revoke(context: Context) {
        prefs(context).edit().putInt(CONSENT, 0).putBoolean(REQUESTED, false).apply()
        context.stopService(Intent(context, OnlineDriverService::class.java))
    }
}

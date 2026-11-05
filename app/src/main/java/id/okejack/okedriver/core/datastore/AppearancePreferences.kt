package id.okejack.okedriver.core.datastore

import android.content.Context
import android.content.SharedPreferences
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Device appearance is independent of authentication and survives sign-out. */
class AppearancePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    val current: DriverUiStyle get() = DriverUiStyle.fromStored(prefs.getString(KEY, null))
    val changes = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY) trySend(current)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(current)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun select(style: DriverUiStyle) { prefs.edit().putString(KEY, style.id).apply() }

    private companion object { const val KEY = "ui_style" }
}

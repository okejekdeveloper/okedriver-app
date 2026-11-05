package id.okejack.okedriver.core.service

import android.content.Context
import android.os.Build
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import id.okejack.okedriver.BuildConfig
import id.okejack.okedriver.R
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.api.FcmApiService
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.TimeZone
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DeviceSessionReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FcmApiService,
    private val session: SessionDataStore
) {
    suspend fun report(token: String) {
        if (token.isBlank()) return
        safeApiCall {
            val installationId = suspendCancellableCoroutine<String> { continuation ->
                FirebaseInstallations.getInstance().id.addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(it)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resumeWithException(it)
                }
            }
            api.submitDevice(JsonObject().apply {
                addProperty("last_app", "ANDROID")
                addProperty("android_app_version", BuildConfig.VERSION_NAME)
                addProperty("android_app_version_code", BuildConfig.VERSION_CODE.toString())
                addProperty("android_version", Build.VERSION.SDK_INT.toString())
                addProperty("android_version_name", Build.VERSION.RELEASE)
                addProperty("android_device_manufacturer", Build.MANUFACTURER)
                addProperty("android_device_model", Build.MODEL)
                addProperty("device_timezone", TimeZone.getDefault().displayName)
                addProperty("android_instance_id", installationId)
            }, token)
        }
        val now = System.currentTimeMillis()
        if (now - session.lastAppReportAt.first() < 360 * 60_000L) return
        // Preserve the legacy configured list. This does not enumerate installed apps.
        val body = JsonObject().apply {
            val apps = com.google.gson.JsonArray()
            context.resources.getStringArray(R.array.bad_apps).forEach(apps::add)
            add("bad_apps", apps)
            add("fake_gps_apps", com.google.gson.JsonArray())
        }
        val result = safeApiCall { api.reportBadApps(body, token) }
        if (result is ApiResponse.Success && result.data.isSuccessful) session.saveAppReportAt(now)
    }
}

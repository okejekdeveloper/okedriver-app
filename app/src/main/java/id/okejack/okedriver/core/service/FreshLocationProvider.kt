package id.okejack.okedriver.core.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import id.okejack.okedriver.core.network.model.ApiResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FreshLocationProvider @Inject constructor(@ApplicationContext private val context: Context) {
    @SuppressLint("MissingPermission")
    suspend fun get(): ApiResponse<DriverLatLng> {
        if (!LocationAccess.hasConsent(context)) return ApiResponse.Error(403, "Persetujuan penggunaan lokasi diperlukan")
        if (!LocationHelper.hasForegroundLocationPermission(context)) return ApiResponse.Error(403, "Izin lokasi diperlukan untuk absensi")
        if (!LocationHelper.isLocationEnabled(context)) return ApiResponse.Error(400, "Aktifkan GPS sebelum melakukan absensi")
        return suspendCancellableCoroutine { continuation ->
            val token = CancellationTokenSource()
            continuation.invokeOnCancellation { token.cancel() }
            val request = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(30_000).setDurationMillis(15_000).build()
            try {
                LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(request, token.token)
                    .addOnSuccessListener { location ->
                        if (continuation.isActive) {
                            val error = when {
                                !LocationAccess.canAccess(context) -> "Persetujuan atau izin lokasi telah dicabut"
                                location == null -> "Tidak dapat memperoleh lokasi terbaru. Coba di area terbuka."
                                (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000 > 30_000 -> "Lokasi GPS terlalu lama. Coba lagi."
                                location.hasAccuracy() && location.accuracy > 150f -> "Akurasi GPS masih rendah. Coba di area terbuka."
                                location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0 ||
                                    (location.latitude == 0.0 && location.longitude == 0.0) -> "Koordinat GPS tidak valid"
                                else -> null
                            }
                            continuation.resume(if (error != null) ApiResponse.Error(400, error)
                                else ApiResponse.Success(DriverLatLng(location!!.latitude, location.longitude)))
                        }
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(ApiResponse.Error(-1, "Gagal memperoleh lokasi terbaru"))
                    }
            } catch (e: SecurityException) {
                if (continuation.isActive) continuation.resume(ApiResponse.Error(403, "Izin lokasi diperlukan untuk absensi"))
            }
        }
    }
}

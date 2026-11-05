package id.okejack.okedriver.core.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    fun hasForegroundLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    fun isLocationEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    fun requestLastKnownLatLng(
        context: Context,
        onResult: (String) -> Unit
    ) {
        requestLastKnownLocation(context) { location ->
            if (location != null) {
                onResult(location.asApiValue())
            } else {
                onResult(DEFAULT_LAT_LNG)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLastKnownLocation(
        context: Context,
        onResult: (DriverLatLng?) -> Unit
    ) {
        if (!LocationAccess.canAccess(context)) {
            onResult(null)
            return
        }

        LocationServices.getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                if (!LocationAccess.canAccess(context)) { onResult(null); return@addOnSuccessListener }
                if (location != null) {
                    onResult(
                        DriverLatLng(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                } else {
                    requestCurrentLocation(context, onResult)
                }
            }
            .addOnFailureListener {
                requestCurrentLocation(context, onResult)
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation(
        context: Context,
        onResult: (DriverLatLng?) -> Unit
    ) {
        if (!LocationAccess.canAccess(context)) { onResult(null); return }
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
            .addOnSuccessListener { location ->
                if (location != null && LocationAccess.canAccess(context)) {
                    onResult(
                        DriverLatLng(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun startOnlineDriverService(context: Context) {
        if (!LocationAccess.canAccess(context) || !isLocationEnabled(context)) return
        LocationAccess.setTrackingRequested(context, true)
        val intent = Intent(context, OnlineDriverService::class.java).apply {
            action = OnlineDriverService.ACTION_START
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } catch (e: SecurityException) {
            Log.w("LocationHelper", "Location service permission unavailable", e)
        } catch (e: IllegalStateException) {
            Log.w("LocationHelper", "Location service cannot start from background", e)
        }
    }

    fun stopOnlineDriverService(context: Context) {
        LocationAccess.setTrackingRequested(context, false)
        context.stopService(Intent(context, OnlineDriverService::class.java))
    }

    private const val DEFAULT_LAT_LNG = "0.0,0.0"
}

data class DriverLatLng(
    val latitude: Double,
    val longitude: Double
) {
    fun asApiValue(): String = "$latitude,$longitude"
}

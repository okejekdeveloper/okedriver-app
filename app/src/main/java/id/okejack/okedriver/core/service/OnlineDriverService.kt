package id.okejack.okedriver.core.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import id.okejack.okedriver.MainActivity
import id.okejack.okedriver.R
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.api.DriverApiService
import id.okejack.okedriver.core.network.model.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import android.os.SystemClock
import javax.inject.Inject

@AndroidEntryPoint
class OnlineDriverService : Service() {
    @Inject lateinit var driverApiService: DriverApiService
    @Inject lateinit var sessionDataStore: SessionDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pushLocationJob: Job? = null
    private var sessionWatchJob: Job? = null
    private var isTracking = false
    @Volatile private var currentLocation: DriverLatLng? = null
    @Volatile private var lastLocationElapsedNanos: Long = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            currentLocation = DriverLatLng(location.latitude, location.longitude)
            lastLocationElapsedNanos = location.elapsedRealtimeNanos
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannelUtils.ensureChannels(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_OFFLINE -> {
                stopTracking()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> startTracking()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        // startForegroundService() obliges us to call startForeground() before any early exit,
        // otherwise the system throws ForegroundServiceDidNotStartInTimeException.
        try {
            startForegroundCompat(buildNotification())
        } catch (e: SecurityException) {
            Log.w(TAG, "Location service permission unavailable", e)
            stopSelf()
            return
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Location service cannot start in current app state", e)
            stopSelf()
            return
        }

        if (!LocationAccess.canAccess(this) || !LocationAccess.trackingRequested(this) || !LocationHelper.isLocationEnabled(this)) {
            Log.w(TAG, "Location permission or provider unavailable; stopping service")
            stopTracking()
            stopSelf()
            return
        }
        if (isTracking) return
        isTracking = true
        startLocationUpdates()
        startLocationPushLoop()
        sessionWatchJob = serviceScope.launch {
            combine(sessionDataStore.token, sessionDataStore.driverOnline) { token, online ->
                !token.isNullOrBlank() && online
            }.collect { shouldTrack ->
                if (!shouldTrack) stopSelf()
            }
        }
    }

    private fun stopTracking() {
        pushLocationJob?.cancel()
        pushLocationJob = null
        sessionWatchJob?.cancel()
        sessionWatchJob = null
        isTracking = false
        currentLocation = null
        lastLocationElapsedNanos = 0L
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else @Suppress("DEPRECATION") stopForeground(true)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                .addOnFailureListener { error ->
                    Log.w(TAG, "Location updates unavailable", error)
                    stopSelf()
                }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && currentLocation == null) {
                    currentLocation = DriverLatLng(location.latitude, location.longitude)
                    lastLocationElapsedNanos = location.elapsedRealtimeNanos
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission revoked", e)
            stopSelf()
        }
    }

    private fun startLocationPushLoop() {
        if (pushLocationJob?.isActive == true) return

        pushLocationJob = serviceScope.launch {
            while (isActive) {
                pushCurrentLocation()
                delay(LOCATION_PUSH_INTERVAL_MS)
            }
        }
    }

    private suspend fun pushCurrentLocation() {
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (!LocationTrackingPolicy.canTrack(LocationAccess.acceptedVersion(this),
                LocationHelper.hasForegroundLocationPermission(this), LocationAccess.trackingRequested(this),
                sessionDataStore.driverOnline.firstOrNull() == true, token)) {
            stopSelf()
            return
        }
        val location = currentLocation ?: return
        if ((SystemClock.elapsedRealtimeNanos() - lastLocationElapsedNanos) / 1_000_000 > 120_000) return

        val body = JsonObject().apply {
            addProperty("last_position", location.asApiValue())
        }

        val result = safeApiCall {
            driverApiService.updateLocation(body, token)
        }
        Log.d(TAG, "Location push completed: ${result.javaClass.simpleName}")
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                LOCATION_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(LOCATION_NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationChannelUtils.locationChannel(this))
            .withDriverBrand(this)
            .setContentTitle("OkeDriver Online")
            .setContentText("Lokasi driver sedang dikirim")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val ACTION_START = "id.okejack.okedriver.onlineservice.START"
        const val ACTION_OFFLINE = "id.okejack.okedriver.onlineservice.OFFLINE"

        private const val LOCATION_NOTIFICATION_ID = 1001
        private const val LOCATION_UPDATE_INTERVAL_MS = 30_000L
        private const val LOCATION_FASTEST_INTERVAL_MS = 10_000L
        private const val LOCATION_PUSH_INTERVAL_MS = 30_000L
        private const val TAG = "OnlineDriverService"
    }
}

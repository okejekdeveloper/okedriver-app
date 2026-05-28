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
import javax.inject.Inject

@AndroidEntryPoint
class OnlineDriverService : Service() {
    @Inject lateinit var driverApiService: DriverApiService
    @Inject lateinit var sessionDataStore: SessionDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pushLocationJob: Job? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            currentLatitude = location.latitude
            currentLongitude = location.longitude
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
        if (!LocationHelper.hasForegroundLocationPermission(this) || !LocationHelper.isLocationEnabled(this)) {
            Log.w(TAG, "Location permission or provider unavailable; stopping service")
            stopSelf()
            return
        }

        startForegroundCompat(buildNotification())
        startLocationUpdates()
        startLocationPushLoop()
    }

    private fun stopTracking() {
        pushLocationJob?.cancel()
        pushLocationJob = null
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
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
        val latitude = currentLatitude ?: return
        val longitude = currentLongitude ?: return
        val token = sessionDataStore.token.firstOrNull().orEmpty()
        if (token.isBlank()) {
            Log.w(TAG, "Missing session token; skip location push")
            return
        }

        val body = JsonObject().apply {
            addProperty("last_position", "$latitude,$longitude")
        }

        val result = safeApiCall {
            driverApiService.updateLocation(body, token)
        }
        Log.d(TAG, "Location push result: $result")
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

        return NotificationCompat.Builder(this, NotificationChannelUtils.LOCATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
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

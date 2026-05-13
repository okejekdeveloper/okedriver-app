package id.okejack.okedriver.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.okejack.okedriver.R

class OnlineDriverService : Service() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelUtils.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationChannelUtils.LOCATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("OkeDriver")
            .setContentText("Driver service is ready")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(LOCATION_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val LOCATION_NOTIFICATION_ID = 1001
    }
}

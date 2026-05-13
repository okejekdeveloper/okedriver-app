package id.okejack.okedriver.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelUtils {
    const val LOCATION_CHANNEL_ID = "okedriver_location"
    const val ORDER_CHANNEL_ID = "okedriver_order"
    const val GENERAL_CHANNEL_ID = "okedriver_general"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(LOCATION_CHANNEL_ID, "Driver location", NotificationManager.IMPORTANCE_LOW),
                NotificationChannel(ORDER_CHANNEL_ID, "Order notifications", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(GENERAL_CHANNEL_ID, "General notifications", NotificationManager.IMPORTANCE_DEFAULT)
            )
        )
    }
}

package id.okejack.okedriver.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.net.Uri
import android.media.AudioAttributes
import id.okejack.okedriver.R

object NotificationChannelUtils {
    const val LOCATION_CHANNEL_ID = "driver_online_channel_1"
    const val ORDER_CHANNEL_ID = "order_reminder_channel_id"
    const val GENERAL_CHANNEL_ID = "general_message"
    const val CHAT_CHANNEL_ID = "order_chat_channel_id"
    const val NEWS_CHANNEL_ID = "news_channel_id"
    const val BONUS_CHANNEL_ID = "bonus_channel_id"

    private fun existingChannel(context: Context, primary: String, vararg previous: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return primary
        val manager = context.getSystemService(NotificationManager::class.java)
        return (listOf(primary) + previous).firstOrNull { manager.getNotificationChannel(it) != null } ?: primary
    }

    fun orderChannel(context: Context) = existingChannel(context, ORDER_CHANNEL_ID, "channels.PENDING_ORDER.1001", "okedriver_order")
    fun locationChannel(context: Context) = existingChannel(context, LOCATION_CHANNEL_ID, "okedriver_location")
    fun chatChannel(context: Context) = existingChannel(context, CHAT_CHANNEL_ID, "order_chat", "okedriver_chat")

    fun soundUri(context: Context, isOrder: Boolean): Uri = Uri.parse(
        "android.resource://${context.packageName}/${if (isOrder) R.raw.onesignal_default_sound else R.raw.notification}"
    )

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(locationChannel(context), "Driver location", NotificationManager.IMPORTANCE_LOW),
                NotificationChannel(orderChannel(context), "Order notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(soundUri(context, true), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
                },
                NotificationChannel(chatChannel(context), "Chat customer", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    setSound(soundUri(context, true), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
                },
                NotificationChannel(NEWS_CHANNEL_ID, "Berita", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(BONUS_CHANNEL_ID, "Bonus", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(GENERAL_CHANNEL_ID, "General notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(soundUri(context, false), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                }
            ).filter { manager.getNotificationChannel(it.id) == null }
        )
    }
}

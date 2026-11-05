package id.okejack.okedriver.core.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.okejack.okedriver.MainActivity
import id.okejack.okedriver.R
import dagger.hilt.android.AndroidEntryPoint
import id.okejack.okedriver.core.common.OkeDriverConstants
import id.okejack.okedriver.core.datastore.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.Notification
import android.app.NotificationManager
import android.net.Uri
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class FCMReceiverHandler : FirebaseMessagingService() {
    @Inject lateinit var sessionDataStore: SessionDataStore
    @Inject lateinit var notificationEvents: DriverNotificationEvents
    @Inject lateinit var orderPushCache: OrderPushCache

    override fun onCreate() {
        super.onCreate()
        NotificationChannelUtils.ensureChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(OkeDriverConstants.TAG, "FCM token refreshed")
        CoroutineScope(Dispatchers.IO).launch {
            sessionDataStore.saveFcmToken(token)
            FcmRegistrationWorker.enqueue(this@FCMReceiverHandler, sessionDataStore.token.first().orEmpty())
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val target = NotificationTarget.fromData(data) ?: return
        if (target.kind == NotificationKind.ORDER) {
            // Keys and sizes only: the payload holds customer details and must not reach the log.
            Log.d(OkeDriverConstants.TAG, "Order push id=${target.id} keys=${data.keys.sorted()} orderJsonChars=${data["order"]?.length ?: 0}")
            data["order"]?.let { orderPushCache.put(target.id, it) }
        }
        notificationEvents.emit(target)
        showNotification(data, target)
    }

    private fun showNotification(data: Map<String, String>, target: NotificationTarget) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(OkeDriverConstants.TAG, "Incoming order notification skipped: permission denied")
            return
        }

        val title = data["message_title"]
            ?: data["title"]
            ?: if (target.kind == NotificationKind.ORDER) "Order masuk" else "OkeDriver"
        val body = data["message_body"]
            ?: data["body"]
            ?: "Ada informasi baru untuk Anda"
        val intent = Intent(this, MainActivity::class.java).apply {
            this.data = Uri.parse("okedriver://notification/${target.kind.name}/${target.id}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_KIND, target.kind.name)
            putExtra(MainActivity.EXTRA_NOTIFICATION_ID, target.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            31 * target.kind.ordinal + target.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val isOrder = target.kind == NotificationKind.ORDER
        val channel = when (target.kind) {
            NotificationKind.ORDER -> NotificationChannelUtils.orderChannel(this)
            NotificationKind.CHAT -> NotificationChannelUtils.chatChannel(this)
            NotificationKind.NEWS -> NotificationChannelUtils.NEWS_CHANNEL_ID
            NotificationKind.BONUS -> NotificationChannelUtils.BONUS_CHANNEL_ID
            else -> NotificationChannelUtils.GENERAL_CHANNEL_ID
        }
        val builder = NotificationCompat.Builder(this, channel)
            .withDriverBrand(this)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (isOrder) NotificationCompat.CATEGORY_EVENT else NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
            .setSound(NotificationChannelUtils.soundUri(this, isOrder || target.kind == NotificationKind.CHAT))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (isOrder) {
            builder.setTimeoutAfter(60_000)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
            ) builder.setFullScreenIntent(pendingIntent, true)
        }
        val notification = builder.build().apply {
            if (isOrder) flags = flags or Notification.FLAG_INSISTENT
        }

        NotificationManagerCompat.from(this).notify(
            target.kind.name, target.id.hashCode(),
            notification
        )
    }

}

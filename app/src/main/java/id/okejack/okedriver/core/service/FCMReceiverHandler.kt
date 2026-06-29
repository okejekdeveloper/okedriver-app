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
import id.okejack.okedriver.feature.auth.domain.usecase.RegisterFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMReceiverHandler : FirebaseMessagingService() {
    @Inject lateinit var sessionDataStore: SessionDataStore
    @Inject lateinit var registerFcmToken: RegisterFcmTokenUseCase

    override fun onCreate() {
        super.onCreate()
        NotificationChannelUtils.ensureChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(OkeDriverConstants.TAG, "FCM token refreshed")
        CoroutineScope(Dispatchers.IO).launch {
            sessionDataStore.saveFcmToken(token)
            registerFcmToken()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        Log.d(OkeDriverConstants.TAG, "FCM message received: ${message.messageId.orEmpty()} data=$data")

        if (data["__type"] == TYPE_JOB_RECEIVED) {
            showIncomingOrderNotification(data)
        }
    }

    private fun showIncomingOrderNotification(data: Map<String, String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(OkeDriverConstants.TAG, "Incoming order notification skipped: permission denied")
            return
        }

        val orderId = data["order_id"]?.toLongOrNull() ?: 0L
        val title = data["message_title"]
            ?: data["title"]
            ?: "Order masuk"
        val body = data["message_body"]
            ?: data["body"]
            ?: "Ada order baru menunggu konfirmasi"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_INCOMING_ORDER_ID, orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, NotificationChannelUtils.ORDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            INCOMING_ORDER_NOTIFICATION_ID + orderId.toInt(),
            notification
        )
    }

    companion object {
        private const val TYPE_JOB_RECEIVED = "job_received"
        private const val INCOMING_ORDER_NOTIFICATION_ID = 3200
    }
}

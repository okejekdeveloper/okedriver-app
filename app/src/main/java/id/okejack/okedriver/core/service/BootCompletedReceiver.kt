package id.okejack.okedriver.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import id.okejack.okedriver.core.datastore.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.okejack.okedriver.MainActivity
import id.okejack.okedriver.R

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject lateinit var sessionDataStore: SessionDataStore
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationChannelUtils.ensureChannels(context)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(8_000) {
                        if (!sessionDataStore.token.first().isNullOrBlank() && sessionDataStore.driverOnline.first() &&
                            LocationAccess.canAccess(context) && LocationAccess.trackingRequested(context)
                        ) {
                            if (LocationHelper.hasBackgroundLocationPermission(context) && LocationHelper.isLocationEnabled(context)) {
                                LocationHelper.startOnlineDriverService(context)
                            } else if (LocationHelper.hasNotificationPermission(context)) {
                                val openApp = PendingIntent.getActivity(context, 1201,
                                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                val notification = NotificationCompat.Builder(context, NotificationChannelUtils.GENERAL_CHANNEL_ID)
                                    .withDriverBrand(context).setContentTitle("OkeDriver")
                                    .setContentText("Buka aplikasi untuk melanjutkan layanan Online setelah restart.")
                                    .setContentIntent(openApp).setAutoCancel(true).build()
                                NotificationManagerCompat.from(context).notify(1201, notification)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BootCompletedReceiver", "Cannot restore driver location service", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

package id.okejack.okedriver.core.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
        Log.d(OkeDriverConstants.TAG, "FCM message received: ${message.messageId.orEmpty()}")
    }
}

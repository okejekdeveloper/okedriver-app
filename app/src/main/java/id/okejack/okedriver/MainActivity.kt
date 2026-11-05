package id.okejack.okedriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import id.okejack.okedriver.core.designsystem.theme.DriverAppearanceHost
import dagger.hilt.android.AndroidEntryPoint
import id.okejack.okedriver.app.navigation.OkeDriverNavGraph
import id.okejack.okedriver.core.datastore.SessionDataStore
import javax.inject.Inject
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import id.okejack.okedriver.core.service.DriverNotificationEvents
import id.okejack.okedriver.core.service.NotificationKind
import id.okejack.okedriver.core.service.NotificationTarget
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import id.okejack.okedriver.core.service.DeviceSessionReporter
import id.okejack.okedriver.core.designsystem.component.LocationConsentHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.DisposableEffect
import id.okejack.okedriver.app.navigation.Route
import android.os.Build
import android.view.WindowManager
import id.okejack.okedriver.core.service.FcmRegistrationWorker

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionDataStore: SessionDataStore
    @Inject lateinit var notificationEvents: DriverNotificationEvents
    @Inject lateinit var deviceSessionReporter: DeviceSessionReporter
    private var notificationTarget by mutableStateOf<NotificationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTarget = if (savedInstanceState != null) {
            readTarget(savedInstanceState.getString(EXTRA_NOTIFICATION_KIND), savedInstanceState.getLong(EXTRA_NOTIFICATION_ID))
        } else targetFromIntent(intent)
        lifecycleScope.launch {
            sessionDataStore.token.collectLatest { token ->
                FcmRegistrationWorker.enqueue(this@MainActivity, token.orEmpty())
                if (!token.isNullOrBlank()) deviceSessionReporter.report(token)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                notificationEvents.events.collect { target ->
                    if (target.kind == NotificationKind.ORDER) notificationTarget = target
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            DriverAppearanceHost {
                LocationConsentHost {
                val navController = rememberNavController()
                val entry by navController.currentBackStackEntryAsState()
                val incoming = entry?.destination?.route == Route.IncomingOrder.TEMPLATE
                DisposableEffect(incoming) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(incoming)
                        setTurnScreenOn(incoming)
                    } else {
                        @Suppress("DEPRECATION")
                        val flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        if (incoming) window.addFlags(flags) else window.clearFlags(flags)
                    }
                    if (incoming) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                }
                OkeDriverNavGraph(
                    navController = navController,
                    sessionDataStore = sessionDataStore,
                    notificationTarget = notificationTarget,
                    onNotificationConsumed = {
                        notificationTarget = null
                        intent?.removeExtra(EXTRA_NOTIFICATION_KIND)
                        intent?.removeExtra(EXTRA_NOTIFICATION_ID)
                        intent?.removeExtra(EXTRA_INCOMING_ORDER_ID)
                    }
                )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTarget = targetFromIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        notificationTarget?.let {
            outState.putString(EXTRA_NOTIFICATION_KIND, it.kind.name)
            outState.putLong(EXTRA_NOTIFICATION_ID, it.id)
        }
        super.onSaveInstanceState(outState)
    }

    private fun targetFromIntent(intent: Intent?): NotificationTarget? {
        if (intent == null) return null
        val legacyOrderId = intent.getLongExtra(EXTRA_INCOMING_ORDER_ID, 0L)
        return readTarget(intent.getStringExtra(EXTRA_NOTIFICATION_KIND), intent.getLongExtra(EXTRA_NOTIFICATION_ID, 0L))
            ?: legacyOrderId.takeIf { it > 0 }?.let { NotificationTarget(NotificationKind.ORDER, it) }
    }

    private fun readTarget(kind: String?, id: Long): NotificationTarget? =
        NotificationKind.entries.firstOrNull { it.name == kind }?.let { NotificationTarget(it, id) }?.takeIf { it.isValid }

    companion object {
        const val EXTRA_INCOMING_ORDER_ID = "incoming_order_id"
        const val EXTRA_NOTIFICATION_KIND = "notification_kind"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}

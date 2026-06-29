package id.okejack.okedriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import dagger.hilt.android.AndroidEntryPoint
import id.okejack.okedriver.app.navigation.OkeDriverNavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incomingOrderId = intent?.getLongExtra(EXTRA_INCOMING_ORDER_ID, 0L) ?: 0L
        enableEdgeToEdge()
        setContent {
            OkedriverTheme {
                val navController = rememberNavController()
                OkeDriverNavGraph(
                    navController = navController,
                    initialIncomingOrderId = incomingOrderId
                )
            }
        }
    }

    companion object {
        const val EXTRA_INCOMING_ORDER_ID = "incoming_order_id"
    }
}

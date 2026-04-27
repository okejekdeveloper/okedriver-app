package id.okejack.okedriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import id.okejack.okedriver.app.navigation.OkeDriverNavGraph
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OkedriverTheme {
                val navController = rememberNavController()
                OkeDriverNavGraph(navController = navController)
            }
        }
    }
}

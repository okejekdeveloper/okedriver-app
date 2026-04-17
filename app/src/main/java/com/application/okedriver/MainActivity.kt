package com.application.okedriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.application.okedriver.core.designsystem.theme.OkedriverTheme
import com.application.okedriver.ui.navigation.OkeDriverNavGraph

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

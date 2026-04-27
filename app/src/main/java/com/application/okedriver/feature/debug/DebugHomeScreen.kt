package id.okejack.okedriver.feature.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.okejack.okedriver.core.network.retrofit.BASE_URL
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver

private val BgDark = Color(0xFF1A1A2E)
private val BgCard = Color(0xFF16213E)
private val Green = Color(0xFF00FF88)
private val Red = Color(0xFFFF4444)
private val Cyan = Color(0xFF00D4FF)
private val Yellow = Color(0xFFFFCC00)
private val Gray = Color(0xFF888888)

@Composable
fun DebugHomeScreen(
    driver: AuthDriver?,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "[DEBUG] Login Response",
            color = Green,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "API: $BASE_URL",
            color = Gray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status card
        DebugCard(title = "STATUS") {
            if (driver != null) {
                Text("✓  success = true", color = Green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("✗  success = false / data null", color = Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Session card
        DebugCard(title = "SESSION TOKEN") {
            Text(
                text = driver?.token?.ifEmpty { "(kosong)" } ?: "(tidak ada)",
                color = Cyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Driver info card
        DebugCard(title = "DRIVER INFO") {
            if (driver != null) {
                DebugRow("id", "${driver.id}")
                DebugRow("name", driver.name)
                DebugRow("email", driver.email)
                DebugRow("phone", driver.phone)
                DebugRow("vehicle_plate", driver.vehiclePlate)
                DebugRow("vehicle_type", "${driver.vehicleType}")
                DebugRow("city_id", "${driver.cityId}")
                DebugRow("driver_type", "${driver.driverType}")
                DebugRow("balance", "${driver.balance}")
                DebugRow("online", "${driver.online}")
                DebugRow("image_url", driver.imageUrl)
            } else {
                Text("(tidak ada data driver)", color = Red, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text("Lanjut ke HomeScreen", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DebugCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(12.dp)
    ) {
        Text(title, color = Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun DebugRow(key: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = key.padEnd(16),
            color = Gray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = ": $value",
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

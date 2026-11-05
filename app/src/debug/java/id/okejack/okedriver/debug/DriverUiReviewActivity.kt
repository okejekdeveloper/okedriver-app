package id.okejack.okedriver.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.app.navigation.*
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.DriverAppearanceController
import id.okejack.okedriver.core.designsystem.theme.LocalDriverAppearance
import id.okejack.okedriver.feature.home.domain.model.*
import id.okejack.okedriver.feature.home.presentation.DriverHomeContent
import id.okejack.okedriver.feature.auth.presentation.login.LoginContent
import id.okejack.okedriver.feature.auth.presentation.login.LoginUiState
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.current.CurrentOrderContent
import id.okejack.okedriver.feature.order.presentation.history.*
import id.okejack.okedriver.feature.order.presentation.incoming.IncomingOrderContent
import id.okejack.okedriver.feature.profile.presentation.*
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.presentation.*
import java.math.BigDecimal

/** Starts actual presentation composables with local fixtures; never instantiates a repository or ViewModel. */
class DriverUiReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val screen = intent.getStringExtra("screen") ?: "home"
        setContent {
            val density = LocalDensity.current
            val scale = intent.getFloatExtra("font_scale", density.fontScale)
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                DriverUiReview(screen, intent.getBooleanExtra("dark", false),
                    intent.getStringExtra("ui_style")?.let(DriverUiStyle::fromStored) ?: DriverUiStyle.SIMPLE)
            }
        }
    }
}

object DriverUiFixtures {
    val driver = HomeDriver(1, "Budi Santoso", "budi@example.test", "081234567890", null, "B 1234 OKE", 0, 1, 0,
        BigDecimal(150000), BigDecimal(85000), false, false, true, false)
    val order = DriverOrder(10245, DriverOrder.STATUS_ASSIGNED, DriverOrder.TYPE_RIDE, "2026-11-02 08:30",
        "-6.200,106.816", "-6.210,106.830", "Stasiun Pasar Senen, pintu keluar utama",
        "Jalan Cempaka Putih Tengah No. 24, Jakarta Pusat", "Tunggu di depan minimarket dekat pintu keluar", "Rumah pagar hitam",
        4200, BigDecimal(18000), BigDecimal(18000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        "Tunai", "Siti Aminah", "081234567891", "Tolong siapkan helm tambahan, ya.", null, emptyList(), "")
}

@Composable
fun DriverUiReview(initialScreen: String = "home", dark: Boolean = false, initialStyle: DriverUiStyle = DriverUiStyle.SIMPLE) {
    var screen by remember { mutableStateOf(initialScreen) }
    var online by remember { mutableStateOf(false) }
    var uiStyle by remember { mutableStateOf(initialStyle) }
    CompositionLocalProvider(LocalDriverAppearance provides DriverAppearanceController(uiStyle) { uiStyle = it }) {
    OkedriverTheme(darkTheme = dark, uiStyle = uiStyle) {
        val selected = when (screen) { "wallet" -> DriverTab.WALLET; "history" -> DriverTab.HISTORY; "account" -> DriverTab.ACCOUNT; else -> DriverTab.HOME }
        Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
            if (screen in listOf("home", "wallet", "history", "account")) DriverNavigationBar(selected.route) {
                screen = when (it) { DriverTab.HOME -> "home"; DriverTab.WALLET -> "wallet"; DriverTab.HISTORY -> "history"; DriverTab.ACCOUNT -> "account" }
            }
        }) { padding -> Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when (screen) {
                "login", "login-error" -> LoginContent(
                    LoginUiState(email = if (screen == "login-error") "driver@example.test" else "",
                        password = if (screen == "login-error") "contoh-sandi" else "",
                        passwordError = if (screen == "login-error") "Email atau kata sandi tidak sesuai." else null),
                    {}, {}, {}, {}, {}, {})
                "current-loading" -> CurrentOrderContent(null, true, false, false, null,
                    { screen = "home" }, {}, {}, {}, {}, {}, {}, {}, showMap = false)
                "incoming" -> IncomingOrderContent(DriverUiFixtures.order, false, null, false, false,
                    { screen = "home" }, {}, { screen = "current" }, {})
                "current" -> CurrentOrderContent(DriverUiFixtures.order, false, false, false, null,
                    { screen = "home" }, {}, {}, {}, {}, {}, {}, {}, showMap = false)
                "wallet" -> WalletContent(WalletUiState(WalletBalance(BigDecimal(150000), BigDecimal(85000))), null,
                    {}, {}, {}, {}, {}, {}, {})
                "history" -> OrderHistoryContent(OrderHistoryUiState(orders = listOf(DriverUiFixtures.order.copy(status = DriverOrder.STATUS_ARRIVED)), hasMore = false),
                    null, {}, {}, { screen = "current" })
                "account" -> ProfileContent(ProfileUiState(), null, {}, {}, {}, {}, {}, {}, {})
                else -> DriverHomeContent(DriverUiFixtures.driver.copy(isOnline = online), HomeSupportInfo(),
                    false, false, false, null, true, true, true,
                    { online = !online }, {}, { screen = "wallet" }, { screen = "current" }, {}, {}, {}, {}, {}, {}, {}, {}, {}) {
                    Surface(Modifier.fillMaxWidth().height(220.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text("Area peta · data simulasi") }
                    }
                }
            }
        } }
    }
    }
}

@Preview(name = "Beranda 360", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun HomePreview() = DriverUiReview()
@Preview(name = "Order masuk 320", widthDp = 320, heightDp = 640, showBackground = true)
@Composable private fun IncomingPreview() = DriverUiReview("incoming")
@Preview(name = "Order berjalan", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun CurrentPreview() = DriverUiReview("current")
@Preview(name = "Saldo", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun WalletPreview() = DriverUiReview("wallet")

@Preview(name = "Dinamis · Beranda", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun DynamicHomePreview() = DriverUiReview(initialStyle = DriverUiStyle.DYNAMIC)
@Preview(name = "Dinamis · Saldo", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun DynamicWalletPreview() = DriverUiReview("wallet", initialStyle = DriverUiStyle.DYNAMIC)
@Preview(name = "Dinamis · Order", widthDp = 360, heightDp = 800, showBackground = true)
@Composable private fun DynamicOrderPreview() = DriverUiReview("incoming", initialStyle = DriverUiStyle.DYNAMIC)

package id.okejack.okedriver

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.app.navigation.*
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.debug.DriverUiFixtures
import id.okejack.okedriver.feature.home.presentation.DriverHomeContent
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import androidx.compose.material3.Text
import id.okejack.okedriver.feature.order.presentation.current.CurrentOrderContent
import id.okejack.okedriver.feature.order.presentation.incoming.IncomingOrderContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Exercise the controls drivers must reach without scrolling, including large system text. */
@RunWith(Parameterized::class)
class DriverUsabilityTest(private val style: DriverUiStyle) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun styles(): List<Array<DriverUiStyle>> = DriverUiStyle.entries.map { arrayOf(it) }
    }
    @get:Rule val compose = createComposeRule()

    private fun compact(content: @Composable () -> Unit) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                OkedriverTheme(darkTheme = false, uiStyle = style) { Box(Modifier.width(320.dp).fillMaxHeight()) { content() } }
            }
        }
    }

    @Test fun primaryNavigationIsLabelledAndSelectableWithLargeText() {
        compact {
            var selected by remember { mutableStateOf(DriverTab.HOME) }
            DriverNavigationBar(selected.route) { selected = it }
        }
        listOf("Beranda", "Saldo", "Riwayat", "Akun").forEach {
            compose.onNodeWithText(it).assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(it, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(layouts) }
            assertEquals("Navigation label must stay on one line at 160% text: $it", 1, layouts.single().lineCount)
        }
        compose.onNodeWithText("Saldo").performClick().assertIsSelected()
    }

    @Test fun acceptStaysVisibleWhenReadingTheBottomOfIncomingOrder() {
        var accepted = 0
        compact { IncomingOrderContent(DriverUiFixtures.order, false, null, false, false, {}, {}, { accepted++ }, {}) }
        compose.onNodeWithText("Terima order").assertIsDisplayed()
        compose.onNodeWithText("Pembayaran").performScrollTo()
        compose.onNodeWithText("Terima order").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, accepted) }
    }

    @Test fun incomingActionsAreDisabledWhileAccepting() {
        compact { IncomingOrderContent(DriverUiFixtures.order, false, null, true, false, {}, {}, {}, {}) }
        compose.onNodeWithText("Tolak").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Terima order").assertIsNotEnabled()
    }

    @Test fun accountMustLoadBeforeDriverCanGoOnline() {
        compact {
            DriverHomeContent(driver = null, supportInfo = HomeSupportInfo(), isLoading = true,
                isTogglingStatus = false, isRegisteringQueue = false, errorMessage = null,
                hasLocationPermission = true, isLocationEnabled = true, hasNotificationPermission = true,
                onToggle = {}, onRefresh = {}, onOpenBalance = {}, onOpenCurrentOrder = {},
                onOpenAttendance = {}, onRegisterQueue = {}, onOpenRecentOrders = {}, onOpenOkeFast = {},
                onOpenNews = {}, onOpenSettings = {}, onRequestLocationPermission = {}, onOpenLocationSettings = {},
                onRequestNotificationPermission = {}) { Text("Peta") }
        }
        compose.onNodeWithText("Memuat akun Anda").assertExists()
        compose.onNodeWithText("Mulai terima order").assertIsNotEnabled()
        compose.onNodeWithText("OFFLINE").assertDoesNotExist()
    }

    @Test fun nextTripActionStaysVisibleAndSupportsScreenReaderActivation() {
        var started = 0
        compact { CurrentOrderContent(DriverUiFixtures.order, false, false, false, null,
            {}, {}, { started++ }, {}, {}, {}, {}, {}, showMap = false) }
        compose.onNodeWithText("Geser untuk mulai").assertIsDisplayed()
        compose.onNodeWithText("Pembayaran").performScrollTo()
        compose.onNodeWithContentDescription("Kembali").assertIsDisplayed()
        compose.onNodeWithText("Geser untuk mulai").assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(1, started) }
    }

    @Test fun processingTripCannotBeActivatedAgain() {
        compact { CurrentOrderContent(DriverUiFixtures.order, false, true, false, null,
            {}, {}, {}, {}, {}, {}, {}, {}, showMap = false) }
        compose.onNodeWithText("Memproses...").assertIsDisplayed().assertIsNotEnabled()
    }
}

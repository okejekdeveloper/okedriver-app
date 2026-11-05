package id.okejack.okedriver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import id.okejack.okedriver.debug.DriverUiFixtures
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.presentation.DriverHomeContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HomeDashboardTest(private val style: DriverUiStyle) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun styles(): List<Array<DriverUiStyle>> = DriverUiStyle.entries.map { arrayOf(it) }
    }

    @get:Rule val compose = createComposeRule()

    @Test fun workToolsRemainReachableWithLargeTextWithoutExpandingASheet() {
        var toggled = 0
        var openedBalance = 0
        var openedSettings = 0
        var openedAttendance = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                OkedriverTheme(uiStyle = style) {
                    Box(Modifier.width(320.dp).height(640.dp)) {
                        DriverHomeContent(
                            driver = DriverUiFixtures.driver,
                            supportInfo = HomeSupportInfo(attendanceEnabled = true),
                            isLoading = false, isTogglingStatus = false, isRegisteringQueue = false,
                            errorMessage = null, hasLocationPermission = true,
                            isLocationEnabled = true, hasNotificationPermission = true,
                            onToggle = { toggled++ }, onRefresh = {},
                            onOpenBalance = { openedBalance++ }, onOpenCurrentOrder = {},
                            onOpenAttendance = { openedAttendance++ }, onRegisterQueue = {},
                            onOpenRecentOrders = {}, onOpenOkeFast = {}, onOpenNews = {},
                            onOpenSettings = { openedSettings++ }, onRequestLocationPermission = {},
                            onOpenLocationSettings = {}, onRequestNotificationPermission = {},
                        ) { Text("Peta posisi") }
                    }
                }
            }
        }

        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Expand)).assertCountEquals(0)
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse)).assertCountEquals(0)
        compose.onNodeWithText("Mulai terima order").assertIsDisplayed().performClick()
        compose.onNodeWithText("Saldo Anda").performScrollTo().performClick()
        compose.onNodeWithText(if (style == DriverUiStyle.SIMPLE) "Absen hari ini" else "Absensi")
            .performScrollTo().performClick()
        compose.onNodeWithText("Pengaturan").performScrollTo().performClick()
        compose.onNodeWithText("Peta posisi").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Perbarui beranda").assertIsDisplayed()
        compose.onNodeWithText("Mulai terima order").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, toggled)
            assertEquals(1, openedBalance)
            assertEquals(1, openedAttendance)
            assertEquals(1, openedSettings)
        }
    }
}

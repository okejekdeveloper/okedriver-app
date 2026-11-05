package id.okejack.okedriver.app.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DriverTabNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var navController: TestNavHostController

    @Before fun setUp() {
        compose.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            // Same shape as OkeDriverNavGraph: Init is the start destination and Home
            // replaces it, so Home is not the graph's start destination.
            NavHost(navController, startDestination = Route.Init.path) {
                composable(Route.Init.path) {}
                DriverTab.entries.forEach { tab -> composable(tab.route) {} }
            }
        }
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.navigate(Route.Home.path) { popUpTo(Route.Init.path) { inclusive = true } }
        }
        compose.waitForIdle()
    }

    private fun select(tab: DriverTab) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { navController.navigateToTab(tab) }
        compose.waitForIdle()    }

    private fun currentRoute() = navController.currentBackStackEntry?.destination?.route

    @Test fun homeTabReturnsToHomeAfterLeavingAnotherTab() {
        select(DriverTab.WALLET)
        select(DriverTab.HISTORY)
        select(DriverTab.HOME)

        assertEquals(Route.Home.path, currentRoute())
    }

    // Home's own menu opens a tab with navigateSingleTop (no popUpTo), unlike the bar.
    private fun openFromHomeMenu(tab: DriverTab) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.navigate(tab.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
        compose.waitForIdle()
    }

    @Test fun homeTabReturnsToHomeAfterATabWasOpenedFromTheHomeMenu() {
        openFromHomeMenu(DriverTab.WALLET)
        select(DriverTab.HISTORY)
        select(DriverTab.HOME)

        assertEquals(Route.Home.path, currentRoute())
        assertEquals(listOf(Route.Home.path), navController.currentBackStack.value.mapNotNull { it.destination.route })
    }

    @Test fun homeTabReturnsToHomeFromTheTabItJustLeft() {
        select(DriverTab.WALLET)
        select(DriverTab.HOME)

        assertEquals(Route.Home.path, currentRoute())
    }

    @Test fun otherTabsStillOpenTheirOwnScreen() {
        select(DriverTab.WALLET)
        select(DriverTab.HOME)
        select(DriverTab.WALLET)
        select(DriverTab.ACCOUNT)

        assertEquals(Route.Profile.path, currentRoute())
    }
}

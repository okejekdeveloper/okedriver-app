package id.okejack.okedriver.app.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToTab(tab: DriverTab) {
    if (tab == DriverTab.HOME) {
        returnToHome()
        return
    }
    navigate(tab.route) {
        popUpTo(Route.Home.path) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Home is the popUpTo target of the other tabs, so the saved state of a popped tab can end up
// registered under Home. Navigating to Home with restoreState would then reopen that tab.
private fun NavHostController.returnToHome() {
    if (!popBackStack(Route.Home.path, inclusive = false)) {
        navigate(Route.Home.path) { launchSingleTop = true }
    }
}

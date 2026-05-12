package id.okejack.okedriver.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.okejack.okedriver.BuildConfig
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.presentation.init.InitScreen
import id.okejack.okedriver.feature.auth.presentation.login.LoginScreen
import id.okejack.okedriver.feature.debug.DebugHomeScreen
import id.okejack.okedriver.feature.home.presentation.HomeScreen

@Composable
fun OkeDriverNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var lastLoggedInDriver by remember { mutableStateOf<AuthDriver?>(null) }

    NavHost(
        navController = navController,
        startDestination = Route.Init.path,
        modifier = modifier
    ) {
        // --- Auth (Phase 1) ---
        composable(Route.Init.path) {
            InitScreen(
                onLoggedIn = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Init.path) { inclusive = true }
                    }
                },
                onNotLoggedIn = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Init.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Login.path) {
            LoginScreen(
                onLoginSuccess = { driver ->
                    if (BuildConfig.DEBUG) {
                        lastLoggedInDriver = driver
                        navController.navigate(Route.DebugHome.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    }
                }
            )
        }

        // --- Debug (DEBUG build only) ---
        composable(Route.DebugHome.path) {
            DebugHomeScreen(
                driver = lastLoggedInDriver,
                onContinue = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.DebugHome.path) { inclusive = true }
                    }
                }
            )
        }

        // --- Main Shell (Phase 2) ---
        composable(Route.Home.path) {
            HomeScreen(
                onLoggedOut = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // --- Order (Phase 3) ---
        composable(
            route = Route.OrderDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.OrderDetail.ARG) { type = NavType.LongType })
        ) {
            // TODO: OrderDetailScreen
        }
        composable(
            route = Route.CurrentOrder.TEMPLATE,
            arguments = listOf(navArgument(Route.CurrentOrder.ARG) { type = NavType.LongType })
        ) {
            // TODO: CurrentOrderScreen
        }
        composable(
            route = Route.Chat.TEMPLATE,
            arguments = listOf(navArgument(Route.Chat.ARG) { type = NavType.LongType })
        ) {
            // TODO: ChatScreen
        }
        composable(
            route = Route.EditShoppingItems.TEMPLATE,
            arguments = listOf(navArgument(Route.EditShoppingItems.ARG) { type = NavType.LongType })
        ) {
            // TODO: EditShoppingItemsScreen
        }
        composable(
            route = Route.OrderPayments.TEMPLATE,
            arguments = listOf(navArgument(Route.OrderPayments.ARG) { type = NavType.LongType })
        ) {
            // TODO: OrderPaymentsScreen
        }

        // --- Wallet (Phase 4) ---
        composable(Route.Transactions.path) { /* TODO */ }
        composable(Route.Withdraw.path) { /* TODO */ }
        composable(Route.TopUp.path) { /* TODO */ }
        composable(Route.BonusBox.path) { /* TODO */ }
        composable(
            route = Route.TopUpDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.TopUpDetail.ARG) { type = NavType.LongType })
        ) { /* TODO */ }

        // --- Profile (Phase 5) ---
        composable(Route.EditProfile.path) { /* TODO */ }
        composable(Route.DriverBid.path) { /* TODO */ }
        composable(Route.ConnectAccount.path) { /* TODO */ }

        // --- News (Phase 6) ---
        composable(Route.News.path) { /* TODO */ }
        composable(
            route = Route.NewsDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.NewsDetail.ARG) { type = NavType.IntType })
        ) { /* TODO */ }

        // --- Misc (Phase 6) ---
        composable(Route.Referral.path) { /* TODO */ }
        composable(Route.AirportAttendance.path) { /* TODO */ }
    }
}

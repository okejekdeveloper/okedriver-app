package com.application.okedriver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.application.okedriver.ui.screens.dashboard.DashboardScreen
import com.application.okedriver.ui.screens.history.HistoryScreen
import com.application.okedriver.ui.screens.history.OrderDetailScreen
import com.application.okedriver.ui.screens.login.LoginScreen
import com.application.okedriver.ui.screens.order.IncomingOrderScreen
import com.application.okedriver.ui.screens.profile.ProfileScreen
import com.application.okedriver.ui.screens.topup.TopUpScreen
import com.application.okedriver.ui.screens.wallet.WalletScreen
import com.application.okedriver.ui.screens.withdraw.WithdrawScreen

/**
 * Root navigation graph for the OkeDriver app.
 *
 * Start destination: [OkeDriverRoute.Login]
 *
 * Full flow:
 *   Login → Dashboard ──► Wallet → TopUp
 *                     ──► History → OrderDetail
 *                     ──► Profile
 *                     ──► IncomingOrder
 */
@Composable
fun OkeDriverNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = OkeDriverRoute.Login.route
    ) {

        // ── Login ─────────────────────────────────────────────────────────────
        composable(OkeDriverRoute.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(OkeDriverRoute.Dashboard.route) {
                        popUpTo(OkeDriverRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard (with side drawer) ──────────────────────────────────────
        composable(OkeDriverRoute.Dashboard.route) {
            DashboardScreen(
                onWalletClick         = { navController.navigate(OkeDriverRoute.Wallet.route) },
                onHistoryClick        = { navController.navigate(OkeDriverRoute.History.route) },
                onProfileClick        = { navController.navigate(OkeDriverRoute.Profile.route) },
                onIncomingOrderClick  = { navController.navigate(OkeDriverRoute.IncomingOrder.route) },
                onLogoutClick         = { navController.navigate(OkeDriverRoute.Login.route) }
            )
        }

        // ── Wallet & Balance ──────────────────────────────────────────────────
        composable(OkeDriverRoute.Wallet.route) {
            WalletScreen(
                onBackClick     = { navController.popBackStack() },
                onTopUpClick    = { navController.navigate(OkeDriverRoute.TopUp.route) },
                onWithdrawClick = { navController.navigate(OkeDriverRoute.Withdraw.route) }
            )
        }

        // ── Top Up Deposit ────────────────────────────────────────────────────
        composable(OkeDriverRoute.TopUp.route) {
            TopUpScreen(
                onBackClick    = { navController.popBackStack() },
                onConfirmClick = { navController.popBackStack() }
            )
        }

        // ── Incoming Order Request ────────────────────────────────────────────
        composable(OkeDriverRoute.IncomingOrder.route) {
            IncomingOrderScreen(
                onAccept  = { navController.popBackStack() },
                onDecline = { navController.popBackStack() }
            )
        }

        // ── Order History (tabs) ──────────────────────────────────────────────
        composable(OkeDriverRoute.History.route) {
            HistoryScreen(
                onBackClick  = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(OkeDriverRoute.OrderDetail.createRoute(orderId))
                }
            )
        }

        // ── Order Detail ──────────────────────────────────────────────────────
        composable(
            route = OkeDriverRoute.OrderDetail.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId     = orderId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Withdraw Request ──────────────────────────────────────────────
        composable(OkeDriverRoute.Withdraw.route) {
            WithdrawScreen(
                currentBalance  = 68_950.0,
                onBackClick     = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(OkeDriverRoute.Profile.route) {
            ProfileScreen(
                onBackClick   = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(OkeDriverRoute.Login.route) {
                        popUpTo(OkeDriverRoute.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

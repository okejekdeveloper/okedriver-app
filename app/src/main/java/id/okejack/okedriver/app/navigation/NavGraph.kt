package id.okejack.okedriver.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
import id.okejack.okedriver.feature.order.presentation.current.CurrentOrderScreen
import id.okejack.okedriver.feature.order.presentation.detail.OrderDetailScreen
import id.okejack.okedriver.feature.order.presentation.chat.ChatScreen
import id.okejack.okedriver.feature.order.presentation.history.OrderHistoryScreen
import id.okejack.okedriver.feature.order.presentation.incoming.IncomingOrderScreen
import id.okejack.okedriver.feature.order.presentation.payments.OrderPaymentsScreen
import id.okejack.okedriver.feature.home.presentation.attendance.AttendanceScreen
import id.okejack.okedriver.feature.order.presentation.shopping.EditShoppingItemsScreen
import id.okejack.okedriver.feature.profile.presentation.ProfileScreen
import id.okejack.okedriver.feature.wallet.presentation.WalletScreen

@Composable
fun OkeDriverNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialIncomingOrderId: Long = 0L
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
                    val destination = initialIncomingOrderId
                        .takeIf { it > 0 }
                        ?.let { Route.IncomingOrder.create(it) }
                        ?: Route.Home.path
                    navController.navigate(destination) {
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
                    // if (BuildConfig.DEBUG) {
                    //     lastLoggedInDriver = driver
                    //     navController.navigate(Route.DebugHome.path) {
                    //         popUpTo(Route.Login.path) { inclusive = true }
                    //     }
                    // } else {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    // }
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
                },
                onOpenBalance = { navController.navigateSingleTop(Route.Balance.path) },
                onOpenHistory = { navController.navigateSingleTop(Route.OrderHistory.path) },
                onOpenProfile = { navController.navigateSingleTop(Route.Profile.path) },
                onOpenSettings = { navController.navigateSingleTop(Route.Settings.path) },
                onOpenNews = { navController.navigateSingleTop(Route.News.path) },
                onOpenCurrentOrder = { orderId ->
                    navController.navigateSingleTop(Route.CurrentOrder.create(orderId))
                },
                onOpenAttendance = { navController.navigateSingleTop(Route.AirportAttendance.path) }
            )
        }
        composable(Route.Balance.path) {
            WalletScreen(
                onBackClick = { navController.popBackStack() },
                onTopUpClick = { navController.navigateSingleTop(Route.TopUp.path) },
                onWithdrawClick = { navController.navigateSingleTop(Route.Withdraw.path) }
            )
        }
        composable(Route.OrderHistory.path) {
            OrderHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigateSingleTop(Route.OrderDetail.create(orderId))
                }
            )
        }
        composable(Route.Profile.path) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Settings.path) {
            PlaceholderScreen(
                title = "Pengaturan",
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Order (Phase 3) ---
        composable(
            route = Route.IncomingOrder.TEMPLATE,
            arguments = listOf(navArgument(Route.IncomingOrder.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.IncomingOrder.ARG) ?: 0L
            IncomingOrderScreen(
                orderId = orderId,
                onClose = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.IncomingOrder.create(orderId)) { inclusive = true }
                    }
                },
                onAccepted = { acceptedOrderId ->
                    navController.navigate(Route.CurrentOrder.create(acceptedOrderId)) {
                        popUpTo(Route.IncomingOrder.create(orderId)) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Route.OrderDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.OrderDetail.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.OrderDetail.ARG) ?: 0L
            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },
                onOpenChat = { id -> navController.navigateSingleTop(Route.Chat.create(id)) },
                onOpenPayments = { id -> navController.navigateSingleTop(Route.OrderPayments.create(id)) }
            )
        }
        composable(
            route = Route.CurrentOrder.TEMPLATE,
            arguments = listOf(navArgument(Route.CurrentOrder.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.CurrentOrder.ARG) ?: 0L
            CurrentOrderScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },
                onOpenChat = { id -> navController.navigateSingleTop(Route.Chat.create(id)) },
                onOpenEditShopping = { id -> navController.navigateSingleTop(Route.EditShoppingItems.create(id)) },
                onOpenPayments = { id -> navController.navigateSingleTop(Route.OrderPayments.create(id)) },
                onOrderFinished = { id ->
                    navController.navigate(Route.OrderDetail.create(id)) {
                        popUpTo(Route.CurrentOrder.create(orderId)) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Route.Chat.TEMPLATE,
            arguments = listOf(navArgument(Route.Chat.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.Chat.ARG) ?: 0L
            ChatScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.EditShoppingItems.TEMPLATE,
            arguments = listOf(navArgument(Route.EditShoppingItems.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.EditShoppingItems.ARG) ?: 0L
            EditShoppingItemsScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.OrderPayments.TEMPLATE,
            arguments = listOf(navArgument(Route.OrderPayments.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong(Route.OrderPayments.ARG) ?: 0L
            OrderPaymentsScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() }
            )
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
        composable(Route.News.path) {
            PlaceholderScreen(
                title = "Berita",
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.NewsDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.NewsDetail.ARG) { type = NavType.IntType })
        ) { /* TODO */ }

        // --- Misc (Phase 6) ---
        composable(Route.Referral.path) { /* TODO */ }
        composable(Route.AirportAttendance.path) {
            AttendanceScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Segera tersedia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

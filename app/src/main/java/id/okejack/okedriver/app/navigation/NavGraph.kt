package id.okejack.okedriver.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import id.okejack.okedriver.feature.order.presentation.okefast.OkeFastScreen
import id.okejack.okedriver.feature.order.presentation.payments.OrderPaymentsScreen
import id.okejack.okedriver.feature.order.presentation.recent.RecentOrderLocationsScreen
import id.okejack.okedriver.feature.home.presentation.attendance.AttendanceScreen
import id.okejack.okedriver.feature.order.presentation.shopping.EditShoppingItemsScreen
import id.okejack.okedriver.feature.profile.presentation.ProfileScreen
import id.okejack.okedriver.feature.profile.presentation.edit.EditProfileScreen
import id.okejack.okedriver.feature.profile.presentation.referral.ReferralScreen
import id.okejack.okedriver.feature.wallet.presentation.TransactionHistoryScreen
import id.okejack.okedriver.feature.wallet.presentation.WalletScreen
import id.okejack.okedriver.feature.wallet.presentation.WithdrawScreen
import id.okejack.okedriver.feature.wallet.presentation.topup.TopUpDetailScreen
import id.okejack.okedriver.feature.wallet.presentation.topup.TopUpHistoryScreen
import id.okejack.okedriver.feature.wallet.presentation.topup.TopUpScreen
import id.okejack.okedriver.feature.wallet.presentation.bonus.BonusBoxScreen
import id.okejack.okedriver.feature.settings.presentation.SettingsScreen
import id.okejack.okedriver.feature.settings.presentation.bid.DriverBidScreen
import id.okejack.okedriver.feature.settings.presentation.connect.ConnectAccountScreen
import id.okejack.okedriver.feature.news.presentation.list.NewsListScreen
import id.okejack.okedriver.feature.news.presentation.detail.NewsDetailScreen
import id.okejack.okedriver.core.datastore.SessionDataStore
import kotlinx.coroutines.flow.filter
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import id.okejack.okedriver.core.service.NotificationTarget
import id.okejack.okedriver.core.service.NotificationKind
import com.google.gson.Gson
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit

@Composable
fun OkeDriverNavGraph(
    navController: NavHostController,
    sessionDataStore: SessionDataStore,
    modifier: Modifier = Modifier,
    notificationTarget: NotificationTarget? = null,
    onNotificationConsumed: () -> Unit = {}
) {
    var lastLoggedInDriver by remember { mutableStateOf<AuthDriver?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val token by sessionDataStore.token.collectAsState(initial = null)
    LaunchedEffect(notificationTarget, backStackEntry, token) {
        val target = notificationTarget
        val currentRoute = backStackEntry?.destination?.route
        if (target != null && target.isValid && !token.isNullOrBlank() && currentRoute != null &&
            currentRoute != Route.Init.path && currentRoute != Route.Login.path
        ) {
            val route = when (target.kind) {
                NotificationKind.ORDER -> Route.IncomingOrder.create(target.id)
                NotificationKind.CHAT -> Route.Chat.create(target.id)
                NotificationKind.NEWS -> Route.NewsDetail.create(target.id.toInt())
                NotificationKind.BONUS -> Route.BonusBox.path
                NotificationKind.GENERAL -> Route.Home.path
            }
            navController.navigateSingleTop(route)
            onNotificationConsumed()
        }
    }

    LaunchedEffect(sessionDataStore) {
        sessionDataStore.sessionExpired.filter { it }.collect {
            navController.returnToLogin()
            sessionDataStore.acknowledgeSessionExpired()
        }
    }

    val rootTab = DriverTab.entries.firstOrNull { it.route == backStackEntry?.destination?.route }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (rootTab != null) DriverNavigationBar(rootTab.route, navController::navigateToTab)
        }
    ) { shellPadding ->
    NavHost(
        navController = navController,
        startDestination = Route.Init.path,
        modifier = modifier.padding(shellPadding).consumeWindowInsets(shellPadding)
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
                    navController.returnToLogin()
                },
                onOpenBalance = { navController.navigateSingleTop(Route.Balance.path) },
                onOpenHistory = { navController.navigateSingleTop(Route.OrderHistory.path) },
                onOpenProfile = { navController.navigateSingleTop(Route.Profile.path) },
                onOpenSettings = { navController.navigateSingleTop(Route.Settings.path) },
                onOpenNews = { navController.navigateSingleTop(Route.News.path) },
                onOpenCurrentOrder = { orderId ->
                    navController.navigateSingleTop(Route.CurrentOrder.create(orderId))
                },
                onOpenAttendance = { navController.navigateSingleTop(Route.AirportAttendance.path) },
                onOpenRecentOrders = { navController.navigateSingleTop(Route.RecentOrderLocations.path) },
                onOpenOkeFast = { navController.navigateSingleTop(Route.OkeFast.path) }
            )
        }
        composable(Route.Balance.path) {
            WalletScreen(
                isRoot = true,
                onBackClick = { navController.popBackStack() },
                onTopUpClick = { navController.navigateSingleTop(Route.TopUp.path) },
                onWithdrawClick = { navController.navigateSingleTop(Route.Withdraw.path) },
                onTransactionsClick = { navController.navigateSingleTop(Route.Transactions.path) },
                onWithdrawHistoryClick = { navController.navigateSingleTop(Route.WithdrawHistory.path) },
                onTopUpHistoryClick = { navController.navigateSingleTop(Route.TopUpHistory.path) },
                onBonusClick = { navController.navigateSingleTop(Route.BonusBox.path) }
            )
        }
        composable(Route.OrderHistory.path) {
            OrderHistoryScreen(
                isRoot = true,
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigateSingleTop(Route.OrderDetail.create(orderId))
                }
            )
        }
        composable(Route.Profile.path) {
            ProfileScreen(
                isRoot = true,
                onSettingsClick = { navController.navigateSingleTop(Route.Settings.path) },
                onNewsClick = { navController.navigateSingleTop(Route.News.path) },
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigateSingleTop(Route.EditProfile.path) },
                onReferralClick = { navController.navigateSingleTop(Route.Referral.path) },
                onConnectAccountClick = { navController.navigateSingleTop(Route.ConnectAccount.path) },
                onLogoutClick = {
                    navController.returnToLogin()
                }
            )
        }
        composable(Route.Settings.path) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onDriverBidClick = { navController.navigateSingleTop(Route.DriverBid.path) },
                onConnectAccountClick = { navController.navigateSingleTop(Route.ConnectAccount.path) }
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
        composable(Route.RecentOrderLocations.path) {
            RecentOrderLocationsScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.OkeFast.path) {
            OkeFastScreen(onBackClick = { navController.popBackStack() })
        }

        // --- Wallet (Phase 4) ---
        composable(Route.Transactions.path) {
            TransactionHistoryScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.WithdrawHistory.path) {
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() },
                withdrawableOnly = true
            )
        }
        composable(Route.Withdraw.path) {
            WithdrawScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.TopUp.path) {
            TopUpScreen(
                onBackClick = { navController.popBackStack() },
                onTopUpCreated = { deposit ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("created_topup", Gson().toJson(deposit))
                    navController.navigate(Route.TopUpDetail.create(deposit.id))
                }
            )
        }
        composable(Route.TopUpHistory.path) {
            TopUpHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onDepositClick = { navController.navigateSingleTop(Route.TopUpDetail.create(it)) }
            )
        }
        composable(Route.BonusBox.path) {
            BonusBoxScreen(onBackClick = { navController.popBackStack() })
        }
        composable(
            route = Route.TopUpDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.TopUpDetail.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            TopUpDetailScreen(
                depositId = backStackEntry.arguments?.getLong(Route.TopUpDetail.ARG) ?: 0L,
                initialDeposit = runCatching {
                    val json = navController.previousBackStackEntry?.savedStateHandle?.get<String>("created_topup")
                    Gson().fromJson(json, TopUpDeposit::class.java)
                }.getOrNull(),
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Profile (Phase 5) ---
        composable(Route.EditProfile.path) {
            EditProfileScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.DriverBid.path) {
            DriverBidScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.ConnectAccount.path) {
            ConnectAccountScreen(onBackClick = { navController.popBackStack() })
        }

        // --- News (Phase 6) ---
        composable(Route.News.path) {
            NewsListScreen(
                onBackClick = { navController.popBackStack() },
                onNewsClick = { navController.navigateSingleTop(Route.NewsDetail.create(it)) }
            )
        }
        composable(
            route = Route.NewsDetail.TEMPLATE,
            arguments = listOf(navArgument(Route.NewsDetail.ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            NewsDetailScreen(
                newsId = backStackEntry.arguments?.getInt(Route.NewsDetail.ARG) ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Misc (Phase 6) ---
        composable(Route.Referral.path) {
            ReferralScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Route.AirportAttendance.path) {
            AttendanceScreen(onBackClick = { navController.popBackStack() })
        }
    }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.returnToLogin() {
    // Saved tab state belongs to this session, including any loaded account balances.
    DriverTab.entries.forEach { clearBackStack(it.route) }
    navigate(Route.Login.path) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

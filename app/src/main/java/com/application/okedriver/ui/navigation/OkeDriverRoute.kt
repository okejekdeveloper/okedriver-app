package com.application.okedriver.ui.navigation

/**
 * All navigation routes in the OkeDriver app.
 * Use [route] as the string key in NavHost / navigate() calls.
 */
sealed class OkeDriverRoute(val route: String) {
    data object Login         : OkeDriverRoute("login")
    data object Dashboard     : OkeDriverRoute("dashboard")
    data object Wallet        : OkeDriverRoute("wallet")
    data object TopUp         : OkeDriverRoute("topup")
    data object IncomingOrder : OkeDriverRoute("incoming_order")
    data object History       : OkeDriverRoute("history")
    data object Profile       : OkeDriverRoute("profile")
    data object Withdraw      : OkeDriverRoute("withdraw")

    /** Order detail — requires orderId argument. Use [createRoute] to build the full path. */
    data object OrderDetail : OkeDriverRoute("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
}

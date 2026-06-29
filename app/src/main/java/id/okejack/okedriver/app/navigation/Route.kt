package id.okejack.okedriver.app.navigation

sealed class Route(val path: String) {

    // --- Auth ---
    data object Init : Route("init")
    data object Login : Route("login")
    data object DebugHome : Route("debug_home")

    // --- Main Shell (Bottom Nav) ---
    data object Home : Route("home")
    data object OrderHistory : Route("order_history")
    data object Balance : Route("balance")
    data object Profile : Route("profile")
    data object Settings : Route("settings")

    // --- Order ---
    data class IncomingOrder(val orderId: Long) : Route("incoming_order/{order_id}") {
        companion object {
            const val TEMPLATE = "incoming_order/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "incoming_order/$orderId"
        }
    }
    data class OrderDetail(val orderId: Long) : Route("order_detail/{order_id}") {
        companion object {
            const val TEMPLATE = "order_detail/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "order_detail/$orderId"
        }
    }
    data class CurrentOrder(val orderId: Long) : Route("current_order/{order_id}") {
        companion object {
            const val TEMPLATE = "current_order/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "current_order/$orderId"
        }
    }
    data class Chat(val orderId: Long) : Route("chat/{order_id}") {
        companion object {
            const val TEMPLATE = "chat/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "chat/$orderId"
        }
    }
    data class EditShoppingItems(val orderId: Long) : Route("edit_shopping/{order_id}") {
        companion object {
            const val TEMPLATE = "edit_shopping/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "edit_shopping/$orderId"
        }
    }
    data class OrderPayments(val orderId: Long) : Route("order_payments/{order_id}") {
        companion object {
            const val TEMPLATE = "order_payments/{order_id}"
            const val ARG = "order_id"

            fun create(orderId: Long): String = "order_payments/$orderId"
        }
    }

    // --- Wallet ---
    data object Transactions : Route("transactions")
    data object Withdraw : Route("withdraw")
    data object TopUp : Route("topup")
    data object BonusBox : Route("bonus_box")
    data class TopUpDetail(val depositId: Long) : Route("topup_detail/{deposit_id}") {
        companion object {
            const val TEMPLATE = "topup_detail/{deposit_id}"
            const val ARG = "deposit_id"
        }
    }

    // --- Profile ---
    data object EditProfile : Route("edit_profile")

    // --- Settings ---
    data object DriverBid : Route("driver_bid")
    data object ConnectAccount : Route("connect_account")

    // --- News ---
    data object News : Route("news")
    data class NewsDetail(val newsId: Int) : Route("news_detail/{news_id}") {
        companion object {
            const val TEMPLATE = "news_detail/{news_id}"
            const val ARG = "news_id"
        }
    }

    // --- Misc ---
    data object Referral : Route("referral")
    data object AirportAttendance : Route("airport_attendance")
}

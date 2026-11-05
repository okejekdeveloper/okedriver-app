package id.okejack.okedriver.feature.settings.domain.model

data class DriverBidPreferences(
    val preferredPayment: Int = NO_LIMIT_PAYMENT,
    val preferredDistance: Int = NO_LIMIT_DISTANCE,
    val rideEnabled: Boolean = true,
    val deliveryEnabled: Boolean = true,
    val shoppingEnabled: Boolean = true,
    val foodEnabled: Boolean = true
) {
    companion object {
        const val NO_LIMIT_PAYMENT = 50_000_000
        const val NO_LIMIT_DISTANCE = 1_000
    }
}

data class ConnectedUser(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String
)

data class DriverSettings(
    val driverId: Long,
    val driverType: Int,
    val bid: DriverBidPreferences,
    val connectedUser: ConnectedUser? = null
)

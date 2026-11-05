package id.okejack.okedriver.feature.home.domain.model

import java.math.BigDecimal

data class HomeDriver(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String?,
    val vehiclePlate: String,
    val vehicleType: Int,
    val cityId: Int,
    val driverType: Int,
    val balance: BigDecimal,
    val withdrawableBalance: BigDecimal,
    val isOnline: Boolean,
    val isOnTrip: Boolean,
    val isAvailableForBidding: Boolean,
    val hasActiveBid: Boolean,
    val isAttendanceEnabled: Boolean = false,
    val isOkefastEnabled: Boolean = false
)

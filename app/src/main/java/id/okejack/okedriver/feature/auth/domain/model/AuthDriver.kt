package id.okejack.okedriver.feature.auth.domain.model

import java.math.BigDecimal

data class AuthDriver(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val imageUrl: String,
    val vehicleType: Int,
    val vehiclePlate: String,
    val cityId: Int,
    val driverType: Int,
    val balance: BigDecimal,
    val withdrawableBalance: BigDecimal,
    val online: Boolean,
    val token: String
)

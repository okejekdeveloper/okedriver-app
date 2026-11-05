package id.okejack.okedriver.feature.profile.domain.model

import java.math.BigDecimal

data class ProfileDriver(
    val id: Long,
    val referralId: String,
    val referralUrl: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val avatarUrl: String?,
    val vehiclePlate: String,
    val vehicleType: Int,
    val vehicleColor: String,
    val vehicleBrand: String,
    val cityId: Int,
    val driverType: Int,
    val balance: BigDecimal,
    val withdrawableBalance: BigDecimal,
    val isOnline: Boolean,
    val bank: BankInfo,
    val createdAt: String
)

data class BankInfo(
    val bankName: String = "",
    val bankAccount: String = "",
    val bankAccountHolder: String = ""
)

data class EditProfileForm(
    val email: String = "",
    val password: String = "",
    val bank: BankInfo = BankInfo()
)

data class ReferralInfo(
    val code: String = "",
    val url: String = "",
    val drivers: List<ProfileDriver> = emptyList()
)

package id.okejack.okedriver.feature.profile.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.profile.domain.model.BankInfo
import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import id.okejack.okedriver.feature.profile.domain.model.ProfileDriver
import id.okejack.okedriver.feature.profile.domain.model.ReferralInfo
import java.math.BigDecimal

data class ProfileResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: ProfileDataDto = ProfileDataDto()
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Request profile gagal")
}

data class ProfileDataDto(
    val driver: ProfileDriverDto = ProfileDriverDto(),
    val drivers: List<ProfileDriverDto> = emptyList(),
    @SerializedName("driver_referral_code") val referralCode: String = "",
    @SerializedName("driver_referral_url") val referralUrl: String = ""
)

data class ProfileDriverDto(
    val id: Long = 0,
    @SerializedName("referral_id") val referralId: String = "",
    @SerializedName("referral_id_url") val referralIdUrl: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val image: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("vehicle_plate") val vehiclePlate: String = "",
    @SerializedName("vehicle_type") val vehicleType: Int = 0,
    @SerializedName("vehicle_color") val vehicleColor: String = "",
    @SerializedName("vehicle_brand") val vehicleBrand: String = "",
    @SerializedName("city_id") val cityId: Int = 0,
    @SerializedName("driver_type") val driverType: Int = 0,
    val balance: BigDecimal = BigDecimal.ZERO,
    @SerializedName("withdrawable_balance") val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    val online: Boolean = false,
    val bank: BankDto = BankDto(),
    @SerializedName("created_at") val createdAt: String = ""
)

data class BankDto(
    @SerializedName("bank_name") val bankName: String = "",
    @SerializedName("bank_account") val bankAccount: String = "",
    @SerializedName("bank_account_holder") val bankAccountHolder: String = ""
)

data class EditProfileRequestDto(
    val email: String,
    val password: String,
    val bank: Map<String, String>,
    @SerializedName("api_token") val apiToken: String = ""
)

fun ProfileDriverDto.toDomain(): ProfileDriver =
    ProfileDriver(
        id = id,
        referralId = referralId,
        referralUrl = referralIdUrl,
        name = name,
        email = email,
        phone = phone,
        address = address,
        avatarUrl = imageUrl.ifBlank { image }.ifBlank { null },
        vehiclePlate = vehiclePlate,
        vehicleType = vehicleType,
        vehicleColor = vehicleColor,
        vehicleBrand = vehicleBrand,
        cityId = cityId,
        driverType = driverType,
        balance = balance,
        withdrawableBalance = withdrawableBalance,
        isOnline = online,
        bank = bank.toDomain(),
        createdAt = createdAt
    )

fun BankDto.toDomain(): BankInfo =
    BankInfo(
        bankName = bankName,
        bankAccount = bankAccount,
        bankAccountHolder = bankAccountHolder
    )

fun EditProfileForm.toRequestDto(apiToken: String = ""): EditProfileRequestDto =
    EditProfileRequestDto(
        email = email,
        password = password,
        apiToken = apiToken,
        bank = mapOf(
            "bank_name" to bank.bankName,
            "bank_account" to bank.bankAccount,
            "bank_account_holder" to bank.bankAccountHolder
        )
    )

fun ProfileDataDto.toReferralInfo(fallbackDriver: ProfileDriver? = null): ReferralInfo =
    ReferralInfo(
        code = referralCode.ifBlank { fallbackDriver?.referralId.orEmpty() },
        url = referralUrl.ifBlank { fallbackDriver?.referralUrl.orEmpty() },
        drivers = drivers.map { it.toDomain() }
    )

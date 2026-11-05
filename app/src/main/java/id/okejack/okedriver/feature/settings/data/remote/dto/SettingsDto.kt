package id.okejack.okedriver.feature.settings.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.settings.domain.model.ConnectedUser
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.model.DriverSettings

data class SettingsResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: SettingsDataDto = SettingsDataDto()
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Request pengaturan gagal")
}

data class SettingsDataDto(val driver: SettingsDriverDto = SettingsDriverDto())

data class SettingsDriverDto(
    val id: Long = 0,
    @SerializedName("driver_type") val driverType: Int = 0,
    @SerializedName("driver_bid") val driverBid: DriverBidDto? = null,
    @SerializedName("user_id") val userId: Long = 0,
    val user: ConnectedUserDto? = null
)

data class DriverBidDto(
    @SerializedName("preferred_distance") val preferredDistance: Int = DriverBidPreferences.NO_LIMIT_DISTANCE,
    @SerializedName("preferred_payments") val preferredPayment: Int = DriverBidPreferences.NO_LIMIT_PAYMENT,
    @SerializedName("prefer_type_ride") val rideEnabled: Boolean = true,
    @SerializedName("prefer_type_delivery") val deliveryEnabled: Boolean = true,
    @SerializedName("prefer_type_shopping") val shoppingEnabled: Boolean = true,
    @SerializedName("prefer_type_food") val foodEnabled: Boolean = true
)

data class ConnectedUserDto(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    @SerializedName("contact_phone") val phone: String = ""
)

data class DriverBidRequestDto(
    @SerializedName("preferred_payments") val preferredPayment: Int,
    @SerializedName("preferred_distance") val preferredDistance: Int,
    @SerializedName("prefer_type_ride") val rideEnabled: Int,
    @SerializedName("prefer_type_delivery") val deliveryEnabled: Int,
    @SerializedName("prefer_type_shopping") val shoppingEnabled: Int,
    @SerializedName("prefer_type_food") val foodEnabled: Int
)

data class ConnectAccountRequestDto(val email: String, val password: String)

fun SettingsDriverDto.toDomain(): DriverSettings {
    val bid = driverBid ?: DriverBidDto()
    return DriverSettings(
        driverId = id,
        driverType = driverType,
        bid = DriverBidPreferences(
            preferredPayment = bid.preferredPayment,
            preferredDistance = bid.preferredDistance,
            rideEnabled = bid.rideEnabled,
            deliveryEnabled = bid.deliveryEnabled,
            shoppingEnabled = bid.shoppingEnabled,
            foodEnabled = bid.foodEnabled
        ),
        connectedUser = user?.takeIf { userId > 0 && it.id > 0 }?.let {
            ConnectedUser(it.id, it.name, it.email, it.phone)
        }
    )
}

fun DriverBidPreferences.toRequestDto(): DriverBidRequestDto = DriverBidRequestDto(
    preferredPayment = preferredPayment,
    preferredDistance = preferredDistance,
    rideEnabled = rideEnabled.toApiInt(),
    deliveryEnabled = deliveryEnabled.toApiInt(),
    shoppingEnabled = shoppingEnabled.toApiInt(),
    foodEnabled = foodEnabled.toApiInt()
)

private fun Boolean.toApiInt(): Int = if (this) 1 else 0

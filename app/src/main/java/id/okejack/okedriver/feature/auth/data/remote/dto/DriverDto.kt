package id.okejack.okedriver.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** DTO yang cocok dengan response JSON dari server OkeJack */
data class DriverDto(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val image: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("vehicle_plate") val vehiclePlate: String = "",
    @SerializedName("vehicle_type") val vehicleType: Int = 0,
    @SerializedName("city_id") val cityId: Int = 0,
    @SerializedName("driver_type") val driverType: Int = 0,
    @SerializedName("balance") val balance: BigDecimal = BigDecimal.ZERO,
    @SerializedName("withdrawable_balance") val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    @SerializedName("online") val online: Boolean = false,
    @SerializedName("enable_driver_queue") val enableDriverQueue: Boolean = false
)

data class LoginResponseData(
    val driver: DriverDto = DriverDto(),
    @SerializedName("session_id") val sessionId: String = ""
)

data class LoginResponseDto(
    val success: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: LoginResponseData = LoginResponseData()
)

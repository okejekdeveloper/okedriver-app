package id.okejack.okedriver.feature.home.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto
import id.okejack.okedriver.feature.home.domain.model.HomeDriver

data class HomeDriverResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeDriverDataDto = HomeDriverDataDto()
) {
    val isSuccessful: Boolean
        get() = success || status

    val errorMessage: String
        get() = ServerErrors.message(error, message, "Request gagal")
}

data class HomeDriverDataDto(
    val driver: DriverDto = DriverDto(),
    @SerializedName("session_id") val sessionId: String = "",
    @SerializedName("is_online") val isOnline: Boolean? = null
)

fun DriverDto.toHomeDriver(): HomeDriver =
    HomeDriver(
        id = id,
        name = name,
        email = email,
        phone = phone,
        avatarUrl = imageUrl.ifBlank { image }.ifBlank { null },
        vehiclePlate = vehiclePlate,
        vehicleType = vehicleType,
        cityId = cityId,
        driverType = driverType,
        balance = balance,
        withdrawableBalance = withdrawableBalance,
        isOnline = online,
        isOnTrip = false,
        isAvailableForBidding = false,
        hasActiveBid = false,
        isAttendanceEnabled = enableDriverAttendance,
        isOkefastEnabled = enableOkefast
    )

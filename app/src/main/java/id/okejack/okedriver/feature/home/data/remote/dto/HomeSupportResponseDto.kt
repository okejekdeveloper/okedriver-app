package id.okejack.okedriver.feature.home.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo

data class HomeMotdResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeMotdDataDto = HomeMotdDataDto()
) {
    val isSuccessful: Boolean
        get() = success || status
}

data class HomeMotdDataDto(
    val motd: String = ""
)

data class HomeQueueResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeQueueDataDto = HomeQueueDataDto()
) {
    val isSuccessful: Boolean
        get() = success || status
}

data class HomeQueueDataDto(
    @SerializedName("driver_queue_number") val queueNumber: Int? = null,
    @SerializedName("queue_terminal") val queueTerminal: String? = null,
    @SerializedName("queue_terminal_total") val queueTerminalTotal: Int? = null,
    @SerializedName(value = "message", alternate = ["queue_message"]) val queueMessage: String = ""
)

data class HomeCurrentOrderResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeCurrentOrderDataDto = HomeCurrentOrderDataDto()
) {
    val isSuccessful: Boolean
        get() = success || status
}

data class HomeCurrentOrderDataDto(
    val order: HomeCurrentOrderDto? = null
)

data class HomeCurrentOrderDto(
    val id: Long = 0
)

data class HomeInitResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeInitDataDto = HomeInitDataDto()
) {
    val isSuccessful: Boolean
        get() = success || status
}

data class HomeInitDataDto(
    @SerializedName("current_stable_build") val currentStableBuild: Int = 0,
    @SerializedName("current_stable_version") val currentStableVersion: String = "",
    @SerializedName(value = "initSettings", alternate = ["init_settings"]) val initSettings: HomeInitSettingsDto = HomeInitSettingsDto()
)

data class HomeInitSettingsDto(
    @SerializedName("current_app_version_code") val currentAppVersionCode: Int = 0,
    @SerializedName("current_app_version") val currentAppVersion: String = "",
    @SerializedName("payment_bidding_range") val paymentBiddingRange: List<Int> = emptyList(),
    @SerializedName("distance_bidding_range") val distanceBiddingRange: List<Int> = emptyList()
)

data class HomeAbsenceResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeAbsenceDataDto = HomeAbsenceDataDto()
) {
    val isSuccessful: Boolean get() = success || status
}

data class HomeAbsenceDataDto(
    @SerializedName("driver_status") val driverStatus: Boolean = false
)

data class HomeSubmitStatusResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: HomeSubmitStatusDataDto = HomeSubmitStatusDataDto()
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal menyimpan absensi")
}

data class HomeSubmitStatusDataDto(
    @SerializedName(value = "driver_queue_number", alternate = ["queue_number"]) val queueNumber: Int? = null,
    @SerializedName("queue_terminal") val queueTerminal: String? = null,
    @SerializedName("queue_terminal_total") val queueTerminalTotal: Int? = null,
    @SerializedName(value = "message", alternate = ["queue_message"]) val queueMessage: String = ""
)

fun toHomeSupportInfo(
    motd: HomeMotdResponseDto?,
    queue: HomeQueueResponseDto?,
    currentOrder: HomeCurrentOrderResponseDto?,
    init: HomeInitResponseDto?,
    absence: HomeAbsenceResponseDto? = null,
    queueEnabled: Boolean = false,
    attendanceEnabled: Boolean = false
): HomeSupportInfo =
    HomeSupportInfo(
        motd = motd?.takeIf { it.isSuccessful }?.data?.motd.orEmpty(),
        queueEnabled = queueEnabled,
        attendanceEnabled = attendanceEnabled,
        hasAttended = absence?.takeIf { it.isSuccessful }?.data?.driverStatus ?: false,
        queueNumber = queue?.takeIf { it.isSuccessful }?.data?.queueNumber,
        queueTerminal = queue?.takeIf { it.isSuccessful }?.data?.queueTerminal,
        queueTerminalTotal = queue?.takeIf { it.isSuccessful }?.data?.queueTerminalTotal,
        queueMessage = queue?.takeIf { it.isSuccessful }?.data?.queueMessage.orEmpty(),
        activeOrderId = currentOrder
            ?.takeIf { it.isSuccessful }
            ?.data
            ?.order
            ?.id
            ?.takeIf { it > 0 },
        latestVersionCode = init
            ?.takeIf { it.isSuccessful }
            ?.data
            ?.initSettings
            ?.currentAppVersionCode
            ?.takeIf { it > 0 }
            ?: init?.takeIf { it.isSuccessful }?.data?.currentStableBuild
            ?: 0,
        latestVersionName = init
            ?.takeIf { it.isSuccessful }
            ?.data
            ?.initSettings
            ?.currentAppVersion
            ?.ifBlank { init.data.currentStableVersion }
            .orEmpty(),
        paymentBiddingRange = init
            ?.takeIf { it.isSuccessful }
            ?.data
            ?.initSettings
            ?.paymentBiddingRange
            .orEmpty(),
        distanceBiddingRange = init
            ?.takeIf { it.isSuccessful }
            ?.data
            ?.initSettings
            ?.distanceBiddingRange
            .orEmpty()
    )

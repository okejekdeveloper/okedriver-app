package id.okejack.okedriver.feature.home.data.remote.dto

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
    @SerializedName("queue_terminal") val queueTerminal: String? = null
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
    @SerializedName("init_settings") val initSettings: HomeInitSettingsDto = HomeInitSettingsDto()
)

data class HomeInitSettingsDto(
    @SerializedName("current_app_version_code") val currentAppVersionCode: Int = 0,
    @SerializedName("current_app_version") val currentAppVersion: String = "",
    @SerializedName("payment_bidding_range") val paymentBiddingRange: List<Int> = emptyList(),
    @SerializedName("distance_bidding_range") val distanceBiddingRange: List<Int> = emptyList()
)

fun toHomeSupportInfo(
    motd: HomeMotdResponseDto?,
    queue: HomeQueueResponseDto?,
    currentOrder: HomeCurrentOrderResponseDto?,
    init: HomeInitResponseDto?
): HomeSupportInfo =
    HomeSupportInfo(
        motd = motd?.takeIf { it.isSuccessful }?.data?.motd.orEmpty(),
        queueNumber = queue?.takeIf { it.isSuccessful }?.data?.queueNumber,
        queueTerminal = queue?.takeIf { it.isSuccessful }?.data?.queueTerminal,
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

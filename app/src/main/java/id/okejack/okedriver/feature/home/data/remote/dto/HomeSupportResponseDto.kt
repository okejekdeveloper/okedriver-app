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

fun toHomeSupportInfo(
    motd: HomeMotdResponseDto?,
    queue: HomeQueueResponseDto?,
    currentOrder: HomeCurrentOrderResponseDto?
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
            ?.takeIf { it > 0 }
    )

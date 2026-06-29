package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.order.domain.model.DriverOrder

data class OrdersListResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: OrdersListDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = error.ifBlank { message.ifBlank { "Gagal memuat riwayat order" } }
}

data class OrdersListDataDto(
    val orders: List<OrderDto>? = null,
    @SerializedName("in_progress") val inProgress: List<OrderDto>? = null
)

fun OrdersListResponseDto.toOrderList(): List<DriverOrder> =
    (data?.orders.orEmpty() + data?.inProgress.orEmpty())
        .map { it.toDomain() }

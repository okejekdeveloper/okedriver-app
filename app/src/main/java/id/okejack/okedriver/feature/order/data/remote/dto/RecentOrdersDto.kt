package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import id.okejack.okedriver.feature.order.domain.model.OrderCoordinate

data class RecentOrdersResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: RecentOrdersDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal memuat lokasi order terbaru")
}

data class RecentOrdersDataDto(
    val coordinates: List<String>? = null
)

fun RecentOrdersResponseDto.toCoordinateList(): List<OrderCoordinate> =
    data?.coordinates.orEmpty().mapNotNull { it.toOrderCoordinateOrNull() }

private fun String.toOrderCoordinateOrNull(): OrderCoordinate? {
    val parts = split(",")
    if (parts.size != 2) return null
    val latitude = parts[0].trim().toDoubleOrNull() ?: return null
    val longitude = parts[1].trim().toDoubleOrNull() ?: return null
    if (latitude == 0.0 && longitude == 0.0) return null
    return OrderCoordinate(latitude = latitude, longitude = longitude)
}

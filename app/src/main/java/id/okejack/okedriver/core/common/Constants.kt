package id.okejack.okedriver.core.common

import java.math.BigDecimal

object OkeDriverConstants {
    const val TAG = "OkeDriver"
    const val CURRENCY_PREFIX = "Rp"
    const val DEFAULT_ZOOM = 15f
    const val PENDING_TIMEOUT_SECONDS = 120L

    val defaultLocation = GeoPoint(latitude = -7.9853297, longitude = 112.6315663)
    val minimumBalance: BigDecimal = BigDecimal.valueOf(5_000)
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

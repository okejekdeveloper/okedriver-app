package id.okejack.okedriver.feature.order.domain.model

import java.math.BigDecimal

data class OrderQrisPayment(
    val qrCodeUrl: String = "",
    val amount: BigDecimal? = null,
    val isPaid: Boolean = false
)

object QrisPaymentPolicy {
    const val FAR_FROM_DESTINATION_METERS = 500f

    fun isFarFromDestination(distanceMeters: Float?): Boolean =
        distanceMeters != null && distanceMeters > FAR_FROM_DESTINATION_METERS
}

package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.order.domain.model.OrderQrisPayment
import java.math.BigDecimal

data class OrderIdBody(@SerializedName("order_id") val orderId: Long)
data class ChatMessageBody(val content: String)

data class OrderQrisResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: OrderQrisDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Permintaan pembayaran gagal")
}

data class OrderQrisDataDto(
    @SerializedName("qr_code_url") val qrCodeUrl: String? = null,
    val amount: BigDecimal? = null,
    @SerializedName("is_paid") val isPaid: Boolean? = null,
    val status: String? = null
) {
    fun toDomain() = OrderQrisPayment(
        qrCodeUrl = qrCodeUrl.orEmpty(),
        amount = amount,
        isPaid = isPaid == true
    )
}

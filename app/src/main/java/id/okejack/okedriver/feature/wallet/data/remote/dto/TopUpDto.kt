package id.okejack.okedriver.feature.wallet.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.wallet.domain.model.PaymentMethod
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.model.VirtualAccount

data class TopUpRequestBody(
    val paymentMethod: Int,
    val amount: String
)

data class DepositIdBody(@SerializedName("deposit_id") val depositId: Long)

data class DepositListBody(
    val page: Int = 1,
    @SerializedName("date_from") val dateFrom: String = "",
    @SerializedName("date_to") val dateTo: String = "",
    val search: String = ""
)

data class TopUpResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: TopUpDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Permintaan top up gagal")
}

data class TopUpDataDto(
    val topup: TopUpDepositDto? = null,
    val vadata: VirtualAccountDto? = null,
    val topups: TopUpListDto? = null,
    @SerializedName("payment_methods") val paymentMethods: List<PaymentMethodDto> = emptyList()
)

data class TopUpListDto(
    val deposits: List<TopUpDepositDto> = emptyList(),
    val total: Int = 0,
    @SerializedName("current_page") val currentPage: Int = 0,
    @SerializedName("last_page") val lastPage: Int = 0
)

data class PaymentMethodDto(
    val type: Int = 0,
    val id: Int = 0,
    val name: String = "",
    @SerializedName("bank_account") val bankAccount: String = "",
    @SerializedName("bank_account_name") val bankAccountName: String = ""
) {
    fun toDomain() = PaymentMethod(
        id = type.takeIf { it > 0 } ?: id,
        name = name,
        accountNumber = bankAccount,
        accountName = bankAccountName
    )
}

data class TopUpDepositDto(
    @SerializedName("deposit_id") val depositId: Long = 0,
    @SerializedName("trans_id") val transactionId: String = "",
    @SerializedName("tanggal") val date: String = "",
    @SerializedName("harga") val amount: String = "",
    val product: String = "",
    @SerializedName("product_detail") val productDetail: String = "",
    @SerializedName("payment") val paymentName: String = "",
    @SerializedName("payment_method") val paymentMethod: String = "",
    @SerializedName("kode_unik") val uniqueCode: String = "",
    @SerializedName("admin_fee") val adminFee: String = "",
    @SerializedName("status_deposit") val status: String = "",
    @SerializedName("status_color") val statusColor: String = "",
    @SerializedName("expired_date") val expiresAt: String = "",
    val note: String = "",
    val total: String = "",
    @SerializedName("qr_string") val qrString: String? = null,
    @SerializedName("qr_code_url") val qrCodeUrl: String? = null
) {
    fun toDomain() = TopUpDeposit(
        id = depositId,
        transactionId = transactionId,
        date = date,
        amount = amount,
        product = product,
        productDetail = productDetail,
        paymentName = paymentName,
        paymentMethod = paymentMethod.toIntOrNull() ?: if (paymentMethod.equals("qris", ignoreCase = true)) 16 else 0,
        uniqueCode = uniqueCode,
        adminFee = adminFee,
        status = status,
        statusColor = statusColor,
        expiresAt = expiresAt,
        note = note,
        total = total,
        qrString = qrString.orEmpty(),
        qrCodeUrl = qrCodeUrl.orEmpty()
    )
}

data class VirtualAccountDto(
    @SerializedName("order_id") val orderId: String = "",
    @SerializedName("va_number") val number: String = "",
    val bank: String = "",
    val amount: String = "",
    @SerializedName("expired_at") val expiresAt: String = "",
    val status: String = "",
    @SerializedName("payment_method") val paymentMethod: Int = 0,
    @SerializedName("account_name") val accountName: String = ""
) {
    fun toDomain() = VirtualAccount(
        orderId, number, bank, amount, expiresAt, status, paymentMethod, accountName
    )
}

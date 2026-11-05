package id.okejack.okedriver.feature.wallet.domain.model

data class PaymentMethod(
    val id: Int,
    val name: String,
    val accountNumber: String = "",
    val accountName: String = ""
)

data class TopUpDeposit(
    val id: Long,
    val transactionId: String = "",
    val date: String = "",
    val amount: String = "",
    val product: String = "",
    val productDetail: String = "",
    val paymentName: String = "",
    val paymentMethod: Int = 0,
    val uniqueCode: String = "",
    val adminFee: String = "",
    val status: String = "",
    val statusColor: String = "",
    val expiresAt: String = "",
    val note: String = "",
    val total: String = "",
    val qrString: String = "",
    val qrCodeUrl: String = ""
) {
    val isQris: Boolean get() = paymentMethod == 16 || qrCodeUrl.isNotBlank() || paymentName.equals("QRIS", ignoreCase = true)
    val isPaid: Boolean get() = status.equals("Berhasil", ignoreCase = true)
    val isTerminal: Boolean get() = isPaid || status.lowercase() in setOf("canceled", "expired", "kadaluarsa")

    fun mergeQr(previous: TopUpDeposit?): TopUpDeposit = if (previous?.id == id) copy(
        qrString = qrString.ifBlank { previous.qrString },
        qrCodeUrl = qrCodeUrl.ifBlank { previous.qrCodeUrl }
    ) else this
}

data class VirtualAccount(
    val orderId: String,
    val number: String,
    val bank: String,
    val amount: String,
    val expiresAt: String,
    val status: String,
    val paymentMethod: Int,
    val accountName: String
)

package id.okejack.okedriver.feature.wallet.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.wallet.domain.model.Transaction
import java.math.BigDecimal

data class TransactionListResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val data: TransactionListDataDto? = null,
    val message: String = ""
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal memuat transaksi")
}

data class TransactionListDataDto(
    val transactions: List<TransactionDto> = emptyList()
)

data class TransactionDto(
    val id: Int = 0,
    val description: String = "",
    val content: BigDecimal = BigDecimal.ZERO,
    val type: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

fun TransactionDto.toDomain(): Transaction =
    Transaction(
        id = id,
        description = description,
        content = content,
        type = type,
        createdAt = createdAt
    )

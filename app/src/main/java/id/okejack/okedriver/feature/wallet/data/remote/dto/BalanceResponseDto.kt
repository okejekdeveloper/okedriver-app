package id.okejack.okedriver.feature.wallet.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import java.math.BigDecimal

data class BalanceResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: BalanceDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal memuat saldo")
}

data class BalanceDataDto(
    val balance: BigDecimal? = null,
    @SerializedName("withdrawable_balance") val withdrawableBalance: BigDecimal? = null
)

fun BalanceDataDto.toDomain(): WalletBalance =
    WalletBalance(
        balance = balance ?: BigDecimal.ZERO,
        withdrawableBalance = withdrawableBalance ?: BigDecimal.ZERO
    )

package id.okejack.okedriver.feature.wallet.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WithdrawRequestBody(
    val amount: BigDecimal,
    @SerializedName("is_convert_to_balance") val isConvertToBalance: Int = 0
)

data class WithdrawResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = ""
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal mengajukan penarikan")
}

data class WithdrawCountResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val data: WithdrawCountDataDto? = null,
    val message: String = ""
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal memuat permintaan penarikan")
}

data class WithdrawCountDataDto(
    val counts: Long = 0
)

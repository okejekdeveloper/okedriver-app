package id.okejack.okedriver.feature.wallet.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.wallet.domain.model.BonusBox
import java.math.BigDecimal

data class OpenBonusBoxBody(@SerializedName("bonus_box_id") val bonusBoxId: Int)

data class BonusBoxResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: BonusBoxDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Bonus tidak dapat dimuat")
}

data class BonusBoxDataDto(
    @SerializedName("bonus_boxes") val bonusBoxes: List<BonusBoxDto> = emptyList(),
    @SerializedName("bonus_box") val bonusBox: BonusBoxDto? = null
)

data class BonusBoxDto(
    val id: Int = 0,
    val type: Int = 0,
    val message: String = "",
    val content: BigDecimal = BigDecimal.ZERO,
    val redeemed: Boolean = false
) {
    fun toDomain() = BonusBox(id, type, message, content, redeemed)
}

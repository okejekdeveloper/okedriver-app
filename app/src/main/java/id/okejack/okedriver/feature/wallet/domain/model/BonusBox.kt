package id.okejack.okedriver.feature.wallet.domain.model

import java.math.BigDecimal

data class BonusBox(
    val id: Int,
    val type: Int,
    val message: String,
    val amount: BigDecimal,
    val redeemed: Boolean
)

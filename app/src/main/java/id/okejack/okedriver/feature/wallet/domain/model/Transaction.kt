package id.okejack.okedriver.feature.wallet.domain.model

import java.math.BigDecimal

data class Transaction(
    val id: Int,
    val description: String,
    val content: BigDecimal,
    val type: String,
    val createdAt: String
) {
    val isCredit: Boolean get() = content > BigDecimal.ZERO
}

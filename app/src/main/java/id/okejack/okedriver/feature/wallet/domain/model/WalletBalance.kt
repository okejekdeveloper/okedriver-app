package id.okejack.okedriver.feature.wallet.domain.model

import java.math.BigDecimal

data class WalletBalance(
    val balance: BigDecimal,
    val withdrawableBalance: BigDecimal
)

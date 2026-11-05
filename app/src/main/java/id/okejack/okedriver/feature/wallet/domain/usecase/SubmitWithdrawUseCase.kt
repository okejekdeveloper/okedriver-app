package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import java.math.BigDecimal
import javax.inject.Inject

class SubmitWithdrawUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(amount: BigDecimal, convertToBalance: Boolean = false): ApiResponse<Unit> =
        repository.submitWithdraw(amount, convertToBalance)
}

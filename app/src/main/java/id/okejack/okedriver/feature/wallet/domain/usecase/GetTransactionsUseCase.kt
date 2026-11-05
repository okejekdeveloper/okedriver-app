package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.Transaction
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(
        page: Int,
        withdrawableOnly: Boolean = false
    ): ApiResponse<List<Transaction>> = repository.getTransactions(page, withdrawableOnly)
}

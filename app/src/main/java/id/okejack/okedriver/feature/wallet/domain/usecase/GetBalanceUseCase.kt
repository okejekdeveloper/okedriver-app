package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetBalanceUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): ApiResponse<WalletBalance> = repository.getBalance()
}

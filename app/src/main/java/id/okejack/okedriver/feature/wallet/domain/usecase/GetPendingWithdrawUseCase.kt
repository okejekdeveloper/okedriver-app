package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetPendingWithdrawUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): ApiResponse<Long> = repository.getPendingWithdrawCount()
}

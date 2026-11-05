package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetBonusBoxesUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke() = repository.getBonusBoxes()
}

class OpenBonusBoxUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(id: Int) = repository.openBonusBox(id)
}

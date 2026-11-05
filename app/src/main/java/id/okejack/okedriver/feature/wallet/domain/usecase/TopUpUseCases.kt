package id.okejack.okedriver.feature.wallet.domain.usecase

import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetPaymentMethodsUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke() = repository.getPaymentMethods()
}

class RequestTopUpUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(paymentMethod: Int, amount: Long) =
        repository.requestTopUp(paymentMethod, amount)
}

class GetTopUpDetailUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(depositId: Long) = repository.getTopUpDetail(depositId)
}

class CreateTopUpPaymentUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(depositId: Long) = repository.createTopUpPayment(depositId)
}

class GetTopUpHistoryUseCase @Inject constructor(private val repository: WalletRepository) {
    suspend operator fun invoke(page: Int = 1, dateFrom: String = "", dateTo: String = "", search: String = "") =
        repository.getTopUpHistory(page, dateFrom, dateTo, search)
}

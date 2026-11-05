package id.okejack.okedriver.feature.wallet.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.Transaction
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.domain.model.PaymentMethod
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.model.VirtualAccount
import id.okejack.okedriver.feature.wallet.domain.model.BonusBox
import java.math.BigDecimal

interface WalletRepository {
    suspend fun getBalance(): ApiResponse<WalletBalance>
    suspend fun getPendingWithdrawCount(): ApiResponse<Long>
    suspend fun submitWithdraw(amount: BigDecimal, convertToBalance: Boolean = false): ApiResponse<Unit>
    suspend fun getTransactions(page: Int, withdrawableOnly: Boolean = false): ApiResponse<List<Transaction>>
    suspend fun getPaymentMethods(): ApiResponse<List<PaymentMethod>>
    suspend fun requestTopUp(paymentMethod: Int, amount: Long): ApiResponse<TopUpDeposit>
    suspend fun getTopUpDetail(depositId: Long): ApiResponse<TopUpDeposit>
    suspend fun createTopUpPayment(depositId: Long): ApiResponse<VirtualAccount>
    suspend fun getTopUpHistory(page: Int, dateFrom: String = "", dateTo: String = "", search: String = ""): ApiResponse<List<TopUpDeposit>>
    suspend fun getBonusBoxes(): ApiResponse<List<BonusBox>>
    suspend fun openBonusBox(id: Int): ApiResponse<BonusBox>
}

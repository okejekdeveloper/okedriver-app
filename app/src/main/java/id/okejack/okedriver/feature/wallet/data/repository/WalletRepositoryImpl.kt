package id.okejack.okedriver.feature.wallet.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.wallet.data.remote.WalletApiService
import id.okejack.okedriver.feature.wallet.data.remote.dto.WithdrawRequestBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.DepositIdBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.DepositListBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.TopUpRequestBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.OpenBonusBoxBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.toDomain
import id.okejack.okedriver.feature.wallet.domain.model.PaymentMethod
import id.okejack.okedriver.feature.wallet.domain.model.Transaction
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.model.VirtualAccount
import id.okejack.okedriver.feature.wallet.domain.model.BonusBox
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WalletRepositoryImpl @Inject constructor(
    private val api: WalletApiService,
    private val sessionDataStore: SessionDataStore
) : WalletRepository {

    private suspend fun token(): String = sessionDataStore.token.firstOrNull().orEmpty()

    override suspend fun getBalance(): ApiResponse<WalletBalance> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return coroutineScope {
            val balanceCall = async { safeApiCall { api.balance(apiToken) } }
            val withdrawableCall = async { safeApiCall { api.withdrawableBalance(apiToken) } }
            val result = balanceCall.await()
            val withdrawableResult = withdrawableCall.await()
            if (withdrawableResult is ApiResponse.Error) return@coroutineScope withdrawableResult
            val withdrawableDto = (withdrawableResult as? ApiResponse.Success)?.data
                ?: return@coroutineScope ApiResponse.Error(422, "Saldo penarikan tidak tersedia")
            if (!withdrawableDto.isSuccessful) return@coroutineScope ApiResponse.Error(400, withdrawableDto.errorMessage, errorCode = withdrawableDto.error)
            val withdrawableBalance = withdrawableDto.data?.balance
                ?: return@coroutineScope ApiResponse.Error(422, "Saldo penarikan tidak tersedia")
            when (result) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful && dto.data != null) {
                    val balance = dto.data.balance
                    if (balance != null) ApiResponse.Success(WalletBalance(balance, withdrawableBalance))
                    else ApiResponse.Error(422, "Saldo tidak tersedia")
                } else {
                    ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
            }
        }
    }

    override suspend fun getPendingWithdrawCount(): ApiResponse<Long> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.countWithdrawRequests(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful && dto.data != null) ApiResponse.Success(dto.data.counts)
                else ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun submitWithdraw(amount: BigDecimal, convertToBalance: Boolean): ApiResponse<Unit> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.submitWithdraw(WithdrawRequestBody(amount, if (convertToBalance) 1 else 0), apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(Unit)
                else ApiResponse.Error(400, dto.errorMessage, errorCode = dto.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getTransactions(page: Int, withdrawableOnly: Boolean): ApiResponse<List<Transaction>> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall {
            if (withdrawableOnly) api.withdrawableTransactions(apiToken, page)
            else api.transactions(apiToken, page)
        }) {
            is ApiResponse.Success -> {
                if (result.data.isSuccessful) ApiResponse.Success(result.data.data?.transactions.orEmpty().map { it.toDomain() })
                else ApiResponse.Error(400, result.data.errorMessage, errorCode = result.data.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getPaymentMethods(): ApiResponse<List<PaymentMethod>> =
        mapTopUpCall(
            call = { api.paymentMethods() },
            transform = { it.paymentMethods.map { method -> method.toDomain() } }
        )

    override suspend fun requestTopUp(
        paymentMethod: Int,
        amount: Long
    ): ApiResponse<TopUpDeposit> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return mapTopUpCall(
            call = { api.requestTopUp(TopUpRequestBody(paymentMethod, amount.toString()), apiToken) },
            transform = { data -> data.topup?.toDomain() ?: error("Data top up tidak tersedia") }
        )
    }

    override suspend fun getTopUpDetail(depositId: Long): ApiResponse<TopUpDeposit> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return mapTopUpCall(
            call = { api.topUpDetail(DepositIdBody(depositId), apiToken) },
            transform = { data -> data.topup?.toDomain() ?: error("Detail top up tidak tersedia") }
        )
    }

    override suspend fun createTopUpPayment(depositId: Long): ApiResponse<VirtualAccount> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return mapTopUpCall(
            call = { api.createTopUpPayment(DepositIdBody(depositId), apiToken) },
            transform = { data ->
                data.vadata?.toDomain()?.takeIf { it.number.isNotBlank() }
                    ?: error("Nomor pembayaran belum tersedia")
            }
        )
    }

    override suspend fun getTopUpHistory(page: Int, dateFrom: String, dateTo: String, search: String): ApiResponse<List<TopUpDeposit>> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return mapTopUpCall(
            call = { api.topUpHistory(DepositListBody(page = page, dateFrom = dateFrom, dateTo = dateTo, search = search), apiToken) },
            transform = { data -> data.topups?.deposits.orEmpty().map { it.toDomain() } }
        )
    }

    override suspend fun getBonusBoxes(): ApiResponse<List<BonusBox>> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.bonusBoxes(apiToken) }) {
            is ApiResponse.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    ApiResponse.Success(response.data?.bonusBoxes.orEmpty().map { it.toDomain() })
                } else ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun openBonusBox(id: Int): ApiResponse<BonusBox> {
        val apiToken = token()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.openBonusBox(id, OpenBonusBoxBody(id), apiToken) }) {
            is ApiResponse.Success -> {
                val response = result.data
                val box = response.data?.bonusBox
                if (response.isSuccessful && box != null) ApiResponse.Success(box.toDomain())
                else ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun <T> mapTopUpCall(
        call: suspend () -> id.okejack.okedriver.feature.wallet.data.remote.dto.TopUpResponseDto,
        transform: (id.okejack.okedriver.feature.wallet.data.remote.dto.TopUpDataDto) -> T
    ): ApiResponse<T> = when (val result = safeApiCall(call)) {
        is ApiResponse.Success -> {
            val response = result.data
            if (!response.isSuccessful) {
                ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
            } else {
                runCatching { transform(response.data ?: error("Data tidak tersedia")) }
                    .fold(
                        onSuccess = { ApiResponse.Success(it) },
                        onFailure = { ApiResponse.Error(422, it.message ?: "Data tidak valid") }
                    )
            }
        }
        is ApiResponse.Error -> result
        ApiResponse.Loading -> ApiResponse.Loading
    }
}

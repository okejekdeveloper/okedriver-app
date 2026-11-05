package id.okejack.okedriver.feature.wallet.data.remote

import id.okejack.okedriver.feature.wallet.data.remote.dto.BalanceResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.TransactionListResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.WithdrawCountResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.WithdrawRequestBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.WithdrawResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.DepositIdBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.DepositListBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.TopUpRequestBody
import id.okejack.okedriver.feature.wallet.data.remote.dto.TopUpResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.BonusBoxResponseDto
import id.okejack.okedriver.feature.wallet.data.remote.dto.OpenBonusBoxBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApiService {
    @GET("driver/balance")
    suspend fun balance(@Query("api_token") apiToken: String): BalanceResponseDto

    @GET("driver/withdrawable-balance")
    suspend fun withdrawableBalance(@Query("api_token") apiToken: String): BalanceResponseDto

    @GET("withdraw-requests")
    suspend fun countWithdrawRequests(@Query("api_token") apiToken: String): WithdrawCountResponseDto

    @POST("submit-withdraw")
    suspend fun submitWithdraw(
        @Body body: WithdrawRequestBody,
        @Query("api_token") apiToken: String
    ): WithdrawResponseDto

    @GET("driver/transactions")
    suspend fun transactions(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): TransactionListResponseDto

    @GET("driver/transactions/withdrawable")
    suspend fun withdrawableTransactions(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): TransactionListResponseDto

    @GET("bonus")
    suspend fun bonusBoxes(@Query("api_token") apiToken: String): BonusBoxResponseDto

    @POST("bonus/open/{id}")
    suspend fun openBonusBox(
        @Path("id") id: Int,
        @Body body: OpenBonusBoxBody,
        @Query("api_token") apiToken: String
    ): BonusBoxResponseDto

    @GET("payment-methods")
    suspend fun paymentMethods(): TopUpResponseDto

    @POST("payment-gateway/driver/topup-request")
    suspend fun requestTopUp(
        @Body body: TopUpRequestBody,
        @Query("api_token") apiToken: String
    ): TopUpResponseDto

    @POST("payment-gateway/driver/deposit-detail")
    suspend fun topUpDetail(
        @Body body: DepositIdBody,
        @Query("api_token") apiToken: String
    ): TopUpResponseDto

    @POST("payment-gateway/driver/topup")
    suspend fun createTopUpPayment(
        @Body body: DepositIdBody,
        @Query("api_token") apiToken: String
    ): TopUpResponseDto

    @POST("payment-gateway/driver/deposit-list")
    suspend fun topUpHistory(
        @Body body: DepositListBody,
        @Query("api_token") apiToken: String
    ): TopUpResponseDto
}

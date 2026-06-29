package id.okejack.okedriver.feature.wallet.data.remote

import id.okejack.okedriver.feature.wallet.data.remote.dto.BalanceResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletApiService {
    @GET("driver/balance")
    suspend fun balance(@Query("api_token") apiToken: String): BalanceResponseDto

    @GET("driver/withdrawable-balance")
    suspend fun withdrawableBalance(@Query("api_token") apiToken: String): BalanceResponseDto
}

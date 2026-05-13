package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BalanceApiService {
    @GET("driver/balance")
    suspend fun balance(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("driver/withdrawable-balance")
    suspend fun withdrawableBalance(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("driver/transactions")
    suspend fun transactions(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): BaseResponse<JsonElement>

    @GET("driver/transactions/withdrawable")
    suspend fun withdrawableTransactions(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): BaseResponse<JsonElement>

    @POST("submit-withdraw")
    suspend fun submitWithdraw(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("withdraw-requests")
    suspend fun withdrawRequests(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("bonus")
    suspend fun bonusBoxes(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("bonus/open/{id}")
    suspend fun openBonusBox(
        @Path("id") id: Long,
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("payments/deposits")
    suspend fun deposits(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("payment-gateway/driver/topup-request")
    suspend fun topUpRequest(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("payment-gateway/driver/topup")
    suspend fun topUp(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("payment-gateway/driver/deposit-detail")
    suspend fun depositDetail(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("payment-gateway/driver/deposit-list")
    suspend fun depositList(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("points/points")
    suspend fun points(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("points/transactions")
    suspend fun pointTransactions(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): BaseResponse<JsonElement>

    @POST("points/redeem")
    suspend fun redeemPoint(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>
}

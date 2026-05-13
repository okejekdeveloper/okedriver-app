package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DriverApiService {
    @GET("driver/info")
    suspend fun driverInfo(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("toggle-status")
    suspend fun toggleStatus(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("driver/update-location")
    suspend fun updateLocation(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("driver/check-absence-status")
    suspend fun checkAttendanceStatus(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("driver/submit-status")
    suspend fun submitAttendanceStatus(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("edit-profile")
    suspend fun editProfile(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("submit-driver-bid")
    suspend fun submitDriverBid(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("session/queue/number")
    suspend fun queueNumber(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("session/queue/register")
    suspend fun registerQueue(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("referral/code")
    suspend fun referralCode(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("referral/drivers")
    suspend fun referralDrivers(@Query("api_token") apiToken: String): BaseResponse<JsonElement>
}

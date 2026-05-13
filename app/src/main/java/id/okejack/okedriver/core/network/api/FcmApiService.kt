package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface FcmApiService {
    @POST("fcm/register")
    suspend fun registerFcmToken(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("fcm/unregister")
    suspend fun unregisterFcmToken(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("onesignal/register")
    suspend fun registerOneSignalId(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("session/device")
    suspend fun submitDevice(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("auto-report/bad-apps")
    suspend fun reportBadApps(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>
}

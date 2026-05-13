package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsApiService {
    @POST("user-connect")
    suspend fun connectUser(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("user-disconnect")
    suspend fun disconnectUser(@Query("api_token") apiToken: String): BaseResponse<JsonElement>
}

package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query
import id.okejack.okedriver.core.network.model.AppInitDataDto

interface CommonApiService {
    @GET("app/init/android")
    suspend fun init(): BaseResponse<AppInitDataDto>

    @GET("payment-methods")
    suspend fun paymentMethods(@Query("api_token") apiToken: String? = null): BaseResponse<JsonElement>

    @GET("motd")
    suspend fun motd(@Query("api_token") apiToken: String): BaseResponse<JsonElement>
}

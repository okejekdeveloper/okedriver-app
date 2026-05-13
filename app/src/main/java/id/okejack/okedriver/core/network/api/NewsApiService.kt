package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun news(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1
    ): BaseResponse<JsonElement>

    @GET("news/{id}")
    suspend fun newsDetail(
        @Path("id") id: Long,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>
}

package id.okejack.okedriver.feature.home.data.remote

import id.okejack.okedriver.feature.home.data.remote.dto.HomeDriverResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.HomeCurrentOrderResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.HomeInitResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.HomeMotdResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.HomeQueueResponseDto
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HomeApiService {
    @GET("app/init/android")
    suspend fun init(): HomeInitResponseDto

    @GET("driver/info")
    suspend fun driverInfo(@Query("api_token") apiToken: String): HomeDriverResponseDto

    @GET("toggle-status")
    suspend fun toggleStatus(@Query("api_token") apiToken: String): HomeDriverResponseDto

    @GET("motd")
    suspend fun motd(@Query("api_token") apiToken: String): HomeMotdResponseDto

    @GET("session/queue/number")
    suspend fun queueNumber(@Query("api_token") apiToken: String): HomeQueueResponseDto

    @POST("session/queue/register")
    suspend fun registerQueue(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): HomeQueueResponseDto

    @GET("order/current-order")
    suspend fun currentOrder(@Query("api_token") apiToken: String): HomeCurrentOrderResponseDto
}

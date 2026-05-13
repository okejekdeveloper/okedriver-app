package id.okejack.okedriver.core.network.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.okejack.okedriver.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("orders")
    suspend fun orders(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1,
        @Query("in_progress") inProgress: Int = 0
    ): BaseResponse<JsonElement>

    @GET("order/view/{id}")
    suspend fun viewOrder(
        @Path("id") id: Long,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("order/current-order")
    suspend fun currentOrder(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("order/pending-order")
    suspend fun pendingOrder(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("order/update-status")
    suspend fun updateStatus(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("order/cancel")
    suspend fun cancelCurrentOrder(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("order/cancel")
    suspend fun cancelOrder(
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("orders/recent-orders")
    suspend fun recentOrders(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @POST("order/shopping/edit/{id}")
    suspend fun editShoppingItems(
        @Path("id") id: Long,
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("order/chats/{id}/messages")
    suspend fun chatMessages(
        @Path("id") id: Long,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @POST("order/chats/{id}/submit")
    suspend fun submitChatMessage(
        @Path("id") id: Long,
        @Body body: JsonObject,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>

    @GET("order/okefast/generate-code")
    suspend fun generateOkeFastCode(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("massage/orders")
    suspend fun massageOrders(@Query("api_token") apiToken: String): BaseResponse<JsonElement>

    @GET("vendors/{id}")
    suspend fun vendor(
        @Path("id") id: Long,
        @Query("api_token") apiToken: String
    ): BaseResponse<JsonElement>
}

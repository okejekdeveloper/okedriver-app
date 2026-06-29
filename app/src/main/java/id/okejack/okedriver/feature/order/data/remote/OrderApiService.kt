package id.okejack.okedriver.feature.order.data.remote

import id.okejack.okedriver.feature.order.data.remote.dto.ChatMessagesResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.EditShoppingItemRequestDto
import id.okejack.okedriver.feature.order.data.remote.dto.EditShoppingItemsResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrderResponseDto
import id.okejack.okedriver.feature.order.data.remote.dto.OrdersListResponseDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
    ): OrdersListResponseDto

    @GET("order/current-order")
    suspend fun currentOrder(@Query("api_token") apiToken: String): OrderResponseDto

    @GET("order/view/{id}")
    suspend fun viewOrder(
        @Path("id") id: Long,
        @Query("api_token") apiToken: String
    ): OrderResponseDto

    @GET("order/pending-order")
    suspend fun pendingOrder(@Query("api_token") apiToken: String): OrderResponseDto

    @GET("order/update-status")
    suspend fun updateCurrentOrderStatus(@Query("api_token") apiToken: String): OrderResponseDto

    @FormUrlEncoded
    @POST("order/cancel")
    suspend fun cancelCurrentOrder(
        @Field("reason_code") reasonCode: Int,
        @Query("api_token") apiToken: String
    ): OrderResponseDto

    @GET("order/chats/{id}/messages")
    suspend fun chatMessages(
        @Path("id") orderId: Long,
        @Query("api_token") apiToken: String
    ): ChatMessagesResponseDto

    @FormUrlEncoded
    @POST("order/chats/{id}/submit")
    suspend fun submitChatMessage(
        @Path("id") orderId: Long,
        @Field("content") content: String,
        @Query("api_token") apiToken: String
    ): ChatMessagesResponseDto

    @POST("order/shopping/edit/{id}")
    suspend fun editShoppingItems(
        @Path("id") orderId: Long,
        @Body body: EditShoppingItemRequestDto,
        @Query("api_token") apiToken: String
    ): EditShoppingItemsResponseDto
}

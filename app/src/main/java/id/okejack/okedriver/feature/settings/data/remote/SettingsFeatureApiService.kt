package id.okejack.okedriver.feature.settings.data.remote

import id.okejack.okedriver.feature.settings.data.remote.dto.ConnectAccountRequestDto
import id.okejack.okedriver.feature.settings.data.remote.dto.DriverBidRequestDto
import id.okejack.okedriver.feature.settings.data.remote.dto.SettingsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsFeatureApiService {
    @GET("driver/info")
    suspend fun driverInfo(@Query("api_token") apiToken: String): SettingsResponseDto

    @POST("submit-driver-bid")
    suspend fun submitDriverBid(
        @Body body: DriverBidRequestDto,
        @Query("api_token") apiToken: String
    ): SettingsResponseDto

    @POST("user-connect")
    suspend fun connectAccount(
        @Body body: ConnectAccountRequestDto,
        @Query("api_token") apiToken: String
    ): SettingsResponseDto

    @GET("user-disconnect")
    suspend fun disconnectAccount(@Query("api_token") apiToken: String): SettingsResponseDto
}

package id.okejack.okedriver.feature.profile.data.remote

import id.okejack.okedriver.feature.profile.data.remote.dto.EditProfileRequestDto
import id.okejack.okedriver.feature.profile.data.remote.dto.ProfileResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ProfileApiService {
    @GET("driver/info")
    suspend fun driverInfo(@Query("api_token") apiToken: String): ProfileResponseDto

    @POST("edit-profile")
    suspend fun editProfile(
        @Body body: EditProfileRequestDto,
        @Query("api_token") apiToken: String
    ): ProfileResponseDto

    @GET("referral/code")
    suspend fun referralCode(@Query("api_token") apiToken: String): ProfileResponseDto

    @GET("referral/drivers")
    suspend fun referralDrivers(
        @QueryMap options: Map<String, String>,
        @Query("api_token") apiToken: String
    ): ProfileResponseDto
}

package id.okejack.okedriver.feature.auth.data.remote

import id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto
import id.okejack.okedriver.feature.auth.data.remote.dto.GoogleLoginRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.LoginRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponseDto

    @POST("login/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginResponseDto
}

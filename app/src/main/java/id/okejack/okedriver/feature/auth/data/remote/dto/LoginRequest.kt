package id.okejack.okedriver.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val email: String,
    @SerializedName("token_id") val idToken: String
)

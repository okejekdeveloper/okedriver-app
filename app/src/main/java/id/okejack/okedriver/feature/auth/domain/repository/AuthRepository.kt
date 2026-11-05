package id.okejack.okedriver.feature.auth.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResponse<AuthDriver>
    suspend fun loginWithGoogle(email: String, idToken: String): ApiResponse<AuthDriver>
    suspend fun isLoggedIn(): Boolean
    suspend fun registerFcmToken(token: String? = null): ApiResponse<Unit>
    suspend fun logout()
}

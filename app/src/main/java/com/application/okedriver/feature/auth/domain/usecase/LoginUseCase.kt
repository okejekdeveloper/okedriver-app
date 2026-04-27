package id.okejack.okedriver.feature.auth.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResponse<AuthDriver> {
        if (email.isBlank() || password.isBlank()) {
            return ApiResponse.Error(400, "Email dan password tidak boleh kosong")
        }
        return repository.login(email.trim(), password)
    }
}

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, idToken: String): ApiResponse<AuthDriver> =
        repository.loginWithGoogle(email, idToken)
}

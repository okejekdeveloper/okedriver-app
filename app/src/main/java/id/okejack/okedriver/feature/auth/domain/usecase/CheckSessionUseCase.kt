package id.okejack.okedriver.feature.auth.domain.usecase

import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean = repository.isLoggedIn()
}

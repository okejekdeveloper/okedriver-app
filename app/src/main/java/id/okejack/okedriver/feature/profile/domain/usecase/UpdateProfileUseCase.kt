package id.okejack.okedriver.feature.profile.domain.usecase

import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import id.okejack.okedriver.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(form: EditProfileForm) = repository.updateProfile(form)
}

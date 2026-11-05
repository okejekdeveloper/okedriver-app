package id.okejack.okedriver.feature.profile.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.profile.domain.model.BankInfo
import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import id.okejack.okedriver.feature.profile.domain.model.ProfileDriver
import id.okejack.okedriver.feature.profile.domain.usecase.GetProfileUseCase
import id.okejack.okedriver.feature.profile.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = getProfileUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            profile = result.data,
                            email = result.data.email,
                            bankName = result.data.bank.bankName,
                            bankAccount = result.data.bank.bankAccount,
                            bankAccountHolder = result.data.bank.bankAccountHolder,
                            isLoading = false
                        )
                    }
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    fun onBankNameChange(value: String) = _state.update { it.copy(bankName = value, errorMessage = null, successMessage = null) }
    fun onBankAccountChange(value: String) = _state.update { it.copy(bankAccount = value.filter(Char::isDigit), errorMessage = null, successMessage = null) }
    fun onBankAccountHolderChange(value: String) = _state.update { it.copy(bankAccountHolder = value, errorMessage = null, successMessage = null) }

    fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val form = EditProfileForm(
                email = current.email,
                password = current.password,
                bank = BankInfo(
                    bankName = current.bankName,
                    bankAccount = current.bankAccount,
                    bankAccountHolder = current.bankAccountHolder
                )
            )
            when (val result = updateProfileUseCase(form)) {
                is ApiResponse.Success -> _state.update {
                    it.copy(
                        profile = result.data,
                        password = "",
                        isSubmitting = false,
                        successMessage = "Profil berhasil disimpan"
                    )
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class EditProfileUiState(
    val profile: ProfileDriver? = null,
    val email: String = "",
    val password: String = "",
    val bankName: String = "",
    val bankAccount: String = "",
    val bankAccountHolder: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

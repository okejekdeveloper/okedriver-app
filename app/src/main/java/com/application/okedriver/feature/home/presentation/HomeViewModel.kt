package id.okejack.okedriver.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.feature.auth.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                logoutUseCase()
            } finally {
                _isLoggingOut.value = false
                onComplete()
            }
        }
    }
}

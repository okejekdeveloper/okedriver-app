package id.okejack.okedriver.feature.auth.presentation.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.feature.auth.domain.usecase.CheckSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InitUiState {
    data object Checking : InitUiState()
    data object LoggedIn : InitUiState()
    data object NotLoggedIn : InitUiState()
}

@HiltViewModel
class InitViewModel @Inject constructor(
    private val checkSession: CheckSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<InitUiState>(InitUiState.Checking)
    val state: StateFlow<InitUiState> = _state

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val loggedIn = checkSession.invoke()
            _state.value = if (loggedIn) InitUiState.LoggedIn else InitUiState.NotLoggedIn
        }
    }
}

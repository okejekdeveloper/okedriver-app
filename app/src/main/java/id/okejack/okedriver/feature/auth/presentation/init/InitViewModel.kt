package id.okejack.okedriver.feature.auth.presentation.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.feature.auth.domain.usecase.CheckSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import id.okejack.okedriver.BuildConfig
import id.okejack.okedriver.core.network.api.CommonApiService
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.AppInitSettingsDto
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.core.datastore.SessionDataStore

sealed class InitUiState {
    data object Checking : InitUiState()
    data object LoggedIn : InitUiState()
    data object NotLoggedIn : InitUiState()
    data class UpdateAvailable(val settings: AppInitSettingsDto, val mandatory: Boolean) : InitUiState()
}

@HiltViewModel
class InitViewModel @Inject constructor(
    private val checkSession: CheckSessionUseCase,
    private val commonApi: CommonApiService,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<InitUiState>(InitUiState.Checking)
    val state: StateFlow<InitUiState> = _state

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val initResult = safeApiCall { commonApi.init() }
            val settings = (initResult as? ApiResponse.Success)?.data?.takeIf { it.isSuccessful }?.data?.initSettings
            if (settings != null) {
                sessionDataStore.saveBiddingRanges(settings.paymentBiddingRange, settings.distanceBiddingRange)
                if (settings.hasUpdate(BuildConfig.VERSION_CODE)) {
                    _state.value = InitUiState.UpdateAvailable(settings, settings.requiresUpdate(BuildConfig.VERSION_CODE))
                    return@launch
                }
            }
            val loggedIn = checkSession.invoke()
            _state.value = if (loggedIn) InitUiState.LoggedIn else InitUiState.NotLoggedIn
        }
    }

    fun skipOptionalUpdate() {
        if ((_state.value as? InitUiState.UpdateAvailable)?.mandatory != false) return
        viewModelScope.launch {
            _state.value = if (checkSession.invoke()) InitUiState.LoggedIn else InitUiState.NotLoggedIn
        }
    }
}

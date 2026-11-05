package id.okejack.okedriver.feature.auth.presentation.login

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import id.okejack.okedriver.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @Before fun setMain() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun resetMain() { Dispatchers.resetMain() }

    private class Repository : AuthRepository {
        val driver = AuthDriver(1, "Driver", "driver@example.test", "081234567890", "", 0, "B 1234 OKE",
            1, 0, BigDecimal.ZERO, BigDecimal.ZERO, false, "test-session")
        var response: ApiResponse<AuthDriver> = ApiResponse.Success(driver)
        var calls = 0
        override suspend fun login(email: String, password: String): ApiResponse<AuthDriver> { calls++; return response }
        override suspend fun loginWithGoogle(email: String, idToken: String): ApiResponse<AuthDriver> = error("Google login is not offered")
        override suspend fun isLoggedIn() = false
        override suspend fun registerFcmToken(token: String?) = ApiResponse.Success(Unit)
        override suspend fun logout() = Unit
    }

    private fun ready(repo: Repository) = LoginViewModel(LoginUseCase(repo)).apply {
        onEmailChange("driver@example.test")
        onPasswordChange("test-password")
    }

    @Test fun emptyFieldsAreReportedBesideTheFieldsWithoutANetworkCall() = runTest {
        val repo = Repository()
        val vm = LoginViewModel(LoginUseCase(repo))
        vm.login(); advanceUntilIdle()
        assertNotNull(vm.state.value.emailError)
        assertNotNull(vm.state.value.passwordError)
        assertFalse(vm.state.value.isLoading)
        assertEquals(0, repo.calls)
    }

    @Test fun rejectedCredentialsStayUnderPasswordUntilEdited() = runTest {
        val repo = Repository().apply { response = ApiResponse.Error(400, "Email atau password tidak sesuai.", "bad_auth") }
        val vm = ready(repo)
        vm.login(); advanceUntilIdle()
        assertEquals("Email atau kata sandi tidak sesuai.", vm.state.value.passwordError)
        assertNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.loginSuccess)
        vm.onPasswordChange("corrected-password")
        assertNull(vm.state.value.passwordError)
        vm.login(); advanceUntilIdle()
        vm.onEmailChange("corrected@example.test")
        assertNull(vm.state.value.passwordError)
    }

    @Test fun networkFailureIsNotReportedAsAWrongPassword() = runTest {
        val repo = Repository().apply { response = ApiResponse.Error(500, "Koneksi gagal") }
        val vm = ready(repo)
        vm.login(); advanceUntilIdle()
        assertEquals("Koneksi gagal", vm.state.value.errorMessage)
        assertNull(vm.state.value.passwordError)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun suspendedAccountKeepsItsActualReason() = runTest {
        val repo = Repository().apply { response = ApiResponse.Error(401, "Akun dinonaktifkan", "user_suspended") }
        val vm = ready(repo)
        vm.login(); advanceUntilIdle()
        assertEquals("Akun dinonaktifkan", vm.state.value.errorMessage)
        assertNull(vm.state.value.passwordError)
    }

    @Test fun successfulPasswordLoginStillCompletesAndPreventsDuplicateRequests() = runTest {
        val repo = Repository()
        val vm = ready(repo)
        vm.login(); vm.login()
        assertTrue(vm.state.value.isLoading)
        advanceUntilIdle()
        assertTrue(vm.state.value.loginSuccess)
        assertEquals(repo.driver, vm.state.value.loggedInDriver)
        assertFalse(vm.state.value.isLoading)
        assertEquals(1, repo.calls)
    }
}

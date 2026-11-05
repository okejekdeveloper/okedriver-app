package id.okejack.okedriver

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import id.okejack.okedriver.core.designsystem.theme.OkedriverTheme
import id.okejack.okedriver.feature.auth.presentation.login.LoginContent
import id.okejack.okedriver.feature.auth.presentation.login.LoginUiState
import id.okejack.okedriver.feature.order.presentation.current.CurrentOrderContent
import org.junit.Rule
import org.junit.Test

class LoginAndOrderLoadingUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun credentialErrorRemainsInTheFormAndGoogleLoginIsAbsent() {
        compose.setContent {
            OkedriverTheme {
                LoginContent(LoginUiState(email = "driver@example.test", password = "wrong",
                    passwordError = "Email atau kata sandi tidak sesuai."), {}, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("Email atau kata sandi tidak sesuai.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kata sandi", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Masuk dengan Google").assertDoesNotExist()
        compose.onNodeWithText("Masuk", substring = false).assertExists().assertIsEnabled()
    }

    @Test fun loadingDoesNotFlashAnUnavailableOrderOrRetryAction() {
        compose.setContent {
            OkedriverTheme {
                CurrentOrderContent(null, true, false, false, null,
                    {}, {}, {}, {}, {}, {}, {}, {}, showMap = false)
            }
        }
        compose.onNodeWithText("Memuat order aktif...").assertIsDisplayed()
        compose.onNodeWithText("Order tidak ditemukan").assertDoesNotExist()
        compose.onNodeWithText("Order belum dapat dimuat").assertDoesNotExist()
        compose.onNodeWithText("Coba lagi").assertDoesNotExist()
    }
}

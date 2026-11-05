package id.okejack.okedriver

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.debug.DriverUiReview
import org.junit.Rule
import org.junit.Test

class AppearanceSwitchUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun switchBothAppearancesAndKeepDriverStatus() {
        compose.setContent { DriverUiReview(initialStyle = DriverUiStyle.DYNAMIC) }
        compose.onNodeWithText("Mulai terima order").performScrollTo().performClick()
        compose.onNodeWithText("Berhenti menerima order").assertExists()

        fun choose(style: DriverUiStyle) {
            compose.onNodeWithText("Akun").performClick()
            compose.onNodeWithText("Tampilan aplikasi").performScrollTo().performClick()
            compose.onNode(hasText(style.label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .performClick().assertIsSelected()
            compose.onNodeWithText("Selesai").performClick()
            compose.onNodeWithText(style.label).assertIsDisplayed()
            compose.onNodeWithText("Beranda").performClick()
            compose.onNodeWithText("Berhenti menerima order").assertExists()
        }
        choose(DriverUiStyle.SIMPLE)
        choose(DriverUiStyle.DYNAMIC)
    }
}

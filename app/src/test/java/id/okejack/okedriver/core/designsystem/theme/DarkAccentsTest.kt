package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import id.okejack.okedriver.core.designsystem.theme.dynamic.DynamicDarkColors
import id.okejack.okedriver.core.designsystem.theme.dynamic.DynamicLightColors
import id.okejack.okedriver.core.designsystem.theme.simple.SimpleDarkColors
import id.okejack.okedriver.core.designsystem.theme.simple.SimpleLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DarkAccentsTest {
    private val dark = mapOf("simple" to SimpleDarkColors, "dynamic" to DynamicDarkColors)
    private val light = mapOf("simple" to SimpleLightColors, "dynamic" to DynamicLightColors)

    /** WCAG contrast ratio; 4.5 is the minimum for normal-size text. */
    private fun contrast(a: Color, b: Color): Double {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test fun darkSchemesAreDetectedAsDarkAndLightOnesAreNot() {
        dark.values.forEach { assertTrue(it.isDark) }
        light.values.forEach { assertFalse(it.isDark) }
    }

    @Test fun darkModeUsesGreenForTheBalanceAndYellowForMenuNames() {
        dark.forEach { (name, scheme) ->
            assertEquals("$name balance", scheme.secondary, scheme.balanceAccent)
            assertEquals("$name menu name", scheme.tertiary, scheme.menuTitleAccent)
        }
    }

    @Test fun lightModeKeepsTheNeutralTextColours() {
        light.forEach { (name, scheme) ->
            assertEquals("$name balance", scheme.onSurface, scheme.balanceAccent)
            assertEquals("$name menu name", scheme.onSurface, scheme.menuTitleAccent)
        }
    }

    @Test fun accentsStayReadableOnTheDarkSurfaces() {
        dark.forEach { (name, scheme) ->
            assertTrue("$name balance ${contrast(scheme.balanceAccent, scheme.surface)}", contrast(scheme.balanceAccent, scheme.surface) >= 4.5)
            assertTrue("$name menu name ${contrast(scheme.menuTitleAccent, scheme.surface)}", contrast(scheme.menuTitleAccent, scheme.surface) >= 4.5)
            assertTrue("$name title ${contrast(scheme.onSurface, scheme.surface)}", contrast(scheme.onSurface, scheme.surface) >= 4.5)
        }
    }

    @Test fun balanceAndMenuNamesAreDistinctFromEachOtherAndFromThePlainText() {
        dark.forEach { (name, scheme) ->
            val colours: List<Color> = listOf(scheme.balanceAccent, scheme.menuTitleAccent, scheme.onSurface)
            assertEquals("$name should use three different colours", 3, colours.toSet().size)
        }
    }
}

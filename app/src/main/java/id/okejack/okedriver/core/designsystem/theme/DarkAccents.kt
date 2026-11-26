package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** True when the page background is dark, whether the system or the in-app appearance choice decided it. */
val ColorScheme.isDark: Boolean get() = background.luminance() < 0.5f

/** The balance reads as a positive figure in dark mode; light mode keeps the neutral text colour. */
val ColorScheme.balanceAccent: Color get() = if (isDark) secondary else onSurface

/** Menu names pick up a warm yellow in dark mode; light mode keeps the normal text colour. */
val ColorScheme.menuTitleAccent: Color get() = if (isDark) tertiary else onSurface

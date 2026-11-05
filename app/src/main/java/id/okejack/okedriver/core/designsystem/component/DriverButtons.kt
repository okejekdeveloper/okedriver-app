package id.okejack.okedriver.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.theme.OkeShapeButton

/** Shared geometry for actions, including screens using the Material button APIs directly. */
@Composable
fun DriverButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = OkeShapeButton, colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit,
) = Button(onClick, modifier.heightIn(min = 48.dp), enabled, shape, colors, elevation,
    border, contentPadding, interactionSource, content)

@Composable
fun DriverOutlinedButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = OkeShapeButton, colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit,
) = OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled, shape, colors, elevation,
    border, contentPadding, interactionSource, content)

@Composable
fun DriverTonalButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = OkeShapeButton, colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit,
) = FilledTonalButton(onClick, modifier.heightIn(min = 48.dp), enabled, shape, colors, elevation,
    border, contentPadding, interactionSource, content)

@Composable
fun DriverTextButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = OkeShapeButton, colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit,
) = TextButton(onClick, modifier.heightIn(min = 48.dp), enabled, shape, colors, elevation,
    border, contentPadding, interactionSource, content)

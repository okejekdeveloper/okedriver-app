package id.okejack.okedriver.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import id.okejack.okedriver.core.designsystem.theme.OkeShapeButton

private val THUMB_SIZE = 56.dp
private val TRACK_HEIGHT = 64.dp

object SlideConfirm {
    const val THRESHOLD = 0.85f

    /** A swipe only confirms once the thumb has travelled [THRESHOLD] of the track; a zero-width track never confirms. */
    fun isConfirmed(offset: Float, maxDrag: Float): Boolean =
        maxDrag > 0f && offset / maxDrag >= THRESHOLD
}

/**
 * Swipe-to-confirm button. Drag the thumb to the right past [SlideConfirm.THRESHOLD] to trigger [onConfirm].
 * Snaps back if released before threshold. Accessibility services can activate it directly, since swiping is
 * not possible there; a deliberate screen-reader activation is not an accidental tap.
 */
@Composable
fun SlidingButton(
    label: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(MaterialTheme.colorScheme.primary),
    thumbIcon: ImageVector = Icons.Rounded.ChevronRight,
    thumbColor: Color = Color.White
) {
    val density = LocalDensity.current
    val thumbSizePx = with(density) { THUMB_SIZE.toPx() }
    // The primary colour is light in dark mode, so white text is not always legible.
    val trackText = if (!enabled) MaterialTheme.colorScheme.onSurface else
        if (gradientColors.first().luminance() > 0.179f) Color.Black else Color.White
    val thumbText = if (thumbColor.luminance() > 0.179f) Color.Black else Color.White

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var confirmed by remember { mutableStateOf(false) }

    val maxDrag = (trackWidthPx - thumbSizePx - 8.dp.value * density.density).coerceAtLeast(0f)
    val progress = if (maxDrag > 0f) (dragOffsetX / maxDrag).coerceIn(0f, 1f) else 0f

    val animatedOffset by animateFloatAsState(
        targetValue = if (confirmed) maxDrag else dragOffsetX,
        animationSpec = tween(durationMillis = if (confirmed) 150 else 80),
        finishedListener = {
            if (confirmed) {
                onConfirm()
                confirmed = false
                dragOffsetX = 0f
            }
        },
        label = "sliding_button"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = (1f - progress * 2f).coerceIn(0f, 1f),
        label = "label_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TRACK_HEIGHT)
            .clip(OkeShapeButton)
            .background(if (enabled) gradientColors.first() else MaterialTheme.colorScheme.surfaceVariant)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                if (enabled) onClick(label = label) { onConfirm(); true }
                else disabled()
            }
    ) {
        // Label
        Text(
            text = label,
            color = trackText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 72.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .alpha(labelAlpha)
        )

        // Thumb
        Box(
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterStart)
                .size(THUMB_SIZE)
                .offset { IntOffset((if (confirmed) animatedOffset else dragOffsetX).roundToInt(), 0) }
                .clip(OkeShapeButton)
                .background(thumbColor.copy(alpha = if (enabled) 1f else 0.5f))
                .then(
                    // Keyed on maxDrag: the track width is 0 on first composition, and a captured 0 would pin the thumb.
                    if (enabled) Modifier.pointerInput(maxDrag) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (SlideConfirm.isConfirmed(dragOffsetX, maxDrag)) {
                                    confirmed = true
                                } else {
                                    dragOffsetX = 0f
                                }
                            },
                            onDragCancel = { dragOffsetX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffsetX = (dragOffsetX + dragAmount).coerceIn(0f, maxDrag)
                            }
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = thumbIcon,
                contentDescription = label,
                tint = if (enabled) thumbText else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

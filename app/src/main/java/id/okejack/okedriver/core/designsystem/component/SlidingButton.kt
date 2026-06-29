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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val THUMB_SIZE = 56.dp
private val TRACK_HEIGHT = 64.dp
private val CONFIRM_THRESHOLD = 0.85f

/**
 * Swipe-to-confirm button. Drag the thumb to the right past [CONFIRM_THRESHOLD] to trigger [onConfirm].
 * Snaps back if released before threshold.
 */
@Composable
fun SlidingButton(
    label: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(Color(0xFF10B981), Color(0xFF059669)),
    thumbIcon: ImageVector = Icons.Rounded.ChevronRight,
    thumbColor: Color = Color.White
) {
    val density = LocalDensity.current
    val thumbSizePx = with(density) { THUMB_SIZE.toPx() }

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
            .height(TRACK_HEIGHT)
            .clip(RoundedCornerShape(TRACK_HEIGHT))
            .background(
                if (enabled) Brush.horizontalGradient(gradientColors)
                else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f)))
            )
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
    ) {
        // Label
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(labelAlpha)
        )

        // Thumb
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(THUMB_SIZE)
                .offset { IntOffset((if (confirmed) animatedOffset else dragOffsetX).roundToInt(), 0) }
                .clip(CircleShape)
                .background(thumbColor.copy(alpha = if (enabled) 1f else 0.5f))
                .then(
                    if (enabled) Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (progress >= CONFIRM_THRESHOLD) {
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
                tint = if (enabled) gradientColors.first() else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

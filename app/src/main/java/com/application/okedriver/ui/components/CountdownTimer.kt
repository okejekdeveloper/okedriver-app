package com.application.okedriver.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.OkePrimary
import com.application.okedriver.core.designsystem.theme.OkePrimaryDark
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush

/**
 * Animated circular countdown timer for the Incoming Order screen.
 *
 * @param totalSeconds  Total countdown duration
 * @param onTimeout     Called when countdown reaches 0
 * @param size          Diameter of the timer circle
 */
@Composable
fun CountdownTimer(
    totalSeconds: Int = 15,
    onTimeout: () -> Unit = {},
    size: Dp = 140.dp,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
    progressColor: Color = OkePrimary
) {
    var timeLeft by remember { mutableIntStateOf(totalSeconds) }
    val animatedProgress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalSeconds.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "countdown_progress"
    )

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1_000L)
            timeLeft--
        }
        onTimeout()
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Outer Glow (Shadow-like)
            drawArc(
                color = progressColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 8f, cap = StrokeCap.Round)
            )

            // Inner Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Gradient
            drawArc(
                brush = Brush.sweepGradient(
                    0f to progressColor.copy(alpha = 0.7f),
                    0.5f to progressColor,
                    1f to progressColor.copy(alpha = 0.7f),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${timeLeft}",
                color = OkePrimaryDark,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "SEC",
                color = OkePrimary.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

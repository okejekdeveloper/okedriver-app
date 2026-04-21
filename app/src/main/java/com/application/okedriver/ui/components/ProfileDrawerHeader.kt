package com.application.okedriver.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Premium profile header for the side navigation drawer.
 * Featuring a gradient avatar ring, pulsing online indicator, and refined typography.
 */
@Composable
fun ProfileDrawerHeader(
    userName: String,
    isVerified: Boolean = true,
    modifier: Modifier = Modifier,
    driverId: String = "ID-88291",
    isOnline: Boolean = true
) {
    // Pulsing animation for the online dot
    val infiniteTransition = rememberInfiniteTransition(label = "online_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OkeSurface)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Avatar with Gradient Ring ─────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(listOf(OkePrimary, OkeSecondary)),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(OkePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar",
                        tint = OkePrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                if (isOnline) {
                    // Pulsing online dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(OkeGreenOnline.copy(alpha = pulseAlpha))
                            .border(2.dp, OkeSurface, CircleShape)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    color = OkeTextPrimary
                )
                Text(
                    text = driverId,
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Online Pill & Verification ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isVerified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(OkeShapeTiny)
                        .background(OkeSuccessBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = OkeSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Verified",
                        color = OkeSuccess,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(OkeShapeChip)
                    .background(if (isOnline) OkeSuccess.copy(alpha = 0.1f) else OkeTextHint.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isOnline) "ONLINE" else "OFFLINE",
                    color = if (isOnline) OkeGreenOnline else OkeTextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        HorizontalDivider(color = OkeDivider.copy(alpha = 0.5f))
    }
}

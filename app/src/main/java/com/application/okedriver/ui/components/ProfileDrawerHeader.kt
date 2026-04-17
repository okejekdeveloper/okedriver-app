package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Light-mode profile header for the side navigation drawer.
 * Simple, clean — avatar + name + divider.
 */
@Composable
fun ProfileDrawerHeader(
    userName: String,
    isVerified: Boolean = true,
    modifier: Modifier = Modifier,
    // unused params kept for API compat
    totalTrips: Int = 0,
    rating: Float = 0f,
    isOnline: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(OkePrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Avatar",
                tint = OkePrimary,
                modifier = Modifier.size(34.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = userName,
                color = OkeTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (isVerified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = OkeSuccess,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Verified Driver",
                        color = OkeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        HorizontalDivider(color = OkeDivider)
    }
}

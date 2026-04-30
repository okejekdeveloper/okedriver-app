package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Modernized drawer menu item with pill-shaped selection, indicator bar, and badge support.
 */
@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    isDanger: Boolean = false,
    badgeCount: Int? = null
) {
    val contentColor = when {
        isDanger    -> OkeDanger
        isSelected  -> OkePrimary
        else        -> OkeTextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(OkeShapeChip)
            .background(
                when {
                    isSelected -> OkePrimary.copy(alpha = 0.08f)
                    isDanger   -> OkeDanger.copy(alpha = 0.04f)
                    else       -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left bar indicator for selected state
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(0.5f)
                .clip(OkeShapeChip)
                .background(if (isSelected) OkePrimary else Color.Transparent)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (badgeCount != null && badgeCount > 0) {
            Box(
                modifier = Modifier
                    .clip(OkeShapeChip)
                    .background(OkePrimary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Modernized section label.
 */
@Composable
fun DrawerSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = OkeTextHint,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Bank payment method card (horizontal carousel item in Top Up screen).
 *
 * @param bankName      Full bank name (e.g. "Bank Central Asia")
 * @param bankShortName Abbreviation (e.g. "BCA")
 * @param brandColor    Bank brand color (see OkeColor for presets)
 * @param isSelected    Whether this bank is currently selected
 * @param onClick       Selection callback
 */
@Composable
fun BankCard(
    bankName: String,
    bankShortName: String,
    brandColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) OkePrimaryContainer else OkeSurface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) OkePrimary else OkeInputBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Brand colour badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brandColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bankShortName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Text(
            text = bankShortName,
            color = if (isSelected) OkePrimary else OkeTextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}

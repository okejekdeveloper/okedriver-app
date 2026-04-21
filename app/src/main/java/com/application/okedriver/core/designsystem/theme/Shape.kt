package com.application.okedriver.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val OkeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ── Semantic shape aliases ────────────────────────────────────────────────────
val OkeShapeCard   = RoundedCornerShape(18.dp)
val OkeShapeInput  = RoundedCornerShape(14.dp)
val OkeShapeButton = RoundedCornerShape(28.dp)   // pill CTA
val OkeShapeChip   = RoundedCornerShape(100.dp)  // fully round pill
val OkeShapeSheet  = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val OkeShapeSmall  = RoundedCornerShape(10.dp)
val OkeShapeTiny   = RoundedCornerShape(6.dp)

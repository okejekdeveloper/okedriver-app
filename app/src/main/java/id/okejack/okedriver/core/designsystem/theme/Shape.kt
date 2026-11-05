package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val OkeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(6.dp),
    medium     = RoundedCornerShape(8.dp),
    large      = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

// ── Semantic shape aliases ────────────────────────────────────────────────────
val OkeShapeCard   = RoundedCornerShape(8.dp)
val OkeShapeInput  = RoundedCornerShape(6.dp)
val OkeShapeButton = RoundedCornerShape(6.dp)
val OkeShapeChip   = RoundedCornerShape(4.dp)
val OkeShapeSheet  = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
val OkeShapeSmall  = RoundedCornerShape(6.dp)
val OkeShapeTiny   = RoundedCornerShape(4.dp)

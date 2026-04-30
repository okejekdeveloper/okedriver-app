package com.application.okedriver.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.OrderHistoryCard
import com.application.okedriver.ui.model.OrderHistorySampleData
import com.application.okedriver.ui.model.OrderStatus

/**
 * Premium modernized Order History screen.
 * Featuring a gradient header, pill-shaped tab switcher, and refined list presentation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default: Finished

    val tabs = listOf("In Progress", "History")
    val allOrders = OrderHistorySampleData.orders
    val filteredOrders by remember(selectedTabIndex) {
        derivedStateOf {
            allOrders.filter {
                if (selectedTabIndex == 0) it.status == OrderStatus.IN_PROGRESS
                else it.status == OrderStatus.FINISHED
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
    ) {
        // ── Premium Gradient Header ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(OkeLoginGradientMid, OkePrimaryDark)
                    )
                )
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Order History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Modern Pill Tab Switcher ──────────────────────────────
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White else Color.Transparent
                                    )
                                    .clickable { selectedTabIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) OkePrimaryDark else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── List Content ──────────────────────────────────────────────────
        AnimatedContent(
            targetState = filteredOrders,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
            },
            label = "list_content",
            modifier = Modifier.weight(1f)
        ) { orders ->
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = OkePrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📦", fontSize = 48.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No orders yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = OkeTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Switch tabs or check back later",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OkeTextSecondary
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, 
                        end = 20.dp, 
                        top = 24.dp, 
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = orders, key = { it.id }) { order ->
                        OrderHistoryCard(
                            order = order,
                            onClick = { onOrderClick(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HistoryScreenPreview() {
    OkedriverTheme { HistoryScreen() }
}

package com.application.okedriver.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Order History screen with two tabs: "In Progress" and "Finished".
 *
 * - Purple TopAppBar + TabRow (unified header)
 * - LazyColumn of [OrderHistoryCard] items
 * - Empty state when no orders match
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default: Finished

    val tabs = listOf("In Progress", "Finished")
    val allOrders = OrderHistorySampleData.orders
    val filteredOrders by remember(selectedTabIndex) {
        derivedStateOf {
            allOrders.filter {
                if (selectedTabIndex == 0) it.status == OrderStatus.IN_PROGRESS
                else it.status == OrderStatus.FINISHED
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Purple unified header (TopAppBar + Tabs) ──────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OkePrimary)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OkePrimary
                )
            )

            // ── Tab Row ───────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = OkePrimary,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    // Custom white underline indicator
                    if (tabPositions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = tabPositions[selectedTabIndex].left)
                                .width(tabPositions[selectedTabIndex].width)
                                .height(3.dp)
                                .background(Color.White)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTabIndex == index)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTabIndex == index)
                                    Color.White else Color.White.copy(alpha = 0.65f)
                            )
                        }
                    )
                }
            }
        }

        // ── List content ──────────────────────────────────────────────────────
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OkeBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "📦", fontSize = 56.sp)
                    Text(
                        text = "No orders found",
                        color = OkeTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Orders will appear here once created",
                        color = OkeTextHint,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OkeBg),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = filteredOrders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onClick = { onOrderClick(order.id) }
                    )
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

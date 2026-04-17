package com.application.okedriver.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.DrawerMenuItem
import com.application.okedriver.ui.components.DrawerSectionLabel
import com.application.okedriver.ui.components.OkeDriverMap
import com.application.okedriver.ui.components.ProfileDrawerHeader

/**
 * Dashboard screen with dark map background + side navigation drawer.
 *
 * - ModalNavigationDrawer wraps the main content
 * - Canvas-drawn fake map (grid roads + purple route)
 * - Floating balance card at top
 * - Bottom info panel with stats
 */
@Composable
fun DashboardScreen(
    onWalletClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onIncomingOrderClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedMenuItem by remember { mutableStateOf("History") }
    var isOnline by remember { mutableStateOf(true) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier              = Modifier.width(280.dp),
                drawerContainerColor  = Color.White,
                drawerContentColor    = OkeTextPrimary
            ) {
                // ── Header ────────────────────────────────────────────────
                ProfileDrawerHeader(
                    userName   = "Admin (test)",
                    isVerified = true
                )

                // ── Menu items ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DrawerMenuItem(
                        icon       = Icons.Rounded.History,
                        label      = "History",
                        isSelected = selectedMenuItem == "History",
                        onClick    = {
                            selectedMenuItem = "History"
                            scope.launch { drawerState.close() }
                            onHistoryClick()
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.Rounded.AccountBalanceWallet,
                        label      = "Balance",
                        isSelected = selectedMenuItem == "Balance",
                        onClick    = {
                            selectedMenuItem = "Balance"
                            scope.launch { drawerState.close() }
                            onWalletClick()
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.Rounded.Campaign,
                        label      = "Announcements",
                        isSelected = selectedMenuItem == "Announcements",
                        onClick    = {
                            selectedMenuItem = "Announcements"
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.Rounded.School,
                        label      = "Tutorial",
                        isSelected = selectedMenuItem == "Tutorial",
                        onClick    = {
                            selectedMenuItem = "Tutorial"
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.Rounded.Person,
                        label      = "Profile",
                        isSelected = selectedMenuItem == "Profile",
                        onClick    = {
                            selectedMenuItem = "Profile"
                            scope.launch { drawerState.close() }
                            onProfileClick()
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.Rounded.Settings,
                        label      = "Settings",
                        isSelected = selectedMenuItem == "Settings",
                        onClick    = {
                            selectedMenuItem = "Settings"
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.AutoMirrored.Rounded.HelpOutline,
                        label      = "Help & Support",
                        isSelected = selectedMenuItem == "Help",
                        onClick    = {
                            selectedMenuItem = "Help"
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                // ── Logout footer ─────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(color = OkeDivider)
                    Spacer(modifier = Modifier.height(4.dp))
                    DrawerMenuItem(
                        icon      = Icons.AutoMirrored.Rounded.ExitToApp,
                        label     = "Logout",
                        isDanger  = true,
                        onClick   = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) {


        // ── Main Dashboard Content ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeDarkBg)
        ) {
        // ── Google Maps background ─────────────────────────────────────
            OkeDriverMap(modifier = Modifier.fillMaxSize())

            // ── Status bar area ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger menu — opens the ModalNavigationDrawer
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OkeDarkSurface.copy(alpha = 0.85f))
                        .clickable { scope.launch { drawerState.open() } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White)
                }

                // Balance pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkeDarkSurface.copy(alpha = 0.85f))
                        .clickable { onWalletClick() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = OkePrimaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Rp 300.000",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OkePrimary)
                        .clickable { onProfileClick() }
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = Color.White)
                }
            }

            // ── Bottom info panel ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(OkeDarkSurface)
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                // Online / Offline toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOnline) "You're Online" else "You're Offline",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isOnline) "Ready to receive orders" else "Go online to receive orders",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { isOnline = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OkePrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DashboardStat(label = "Today's Orders", value = "12")
                    DashboardStat(label = "Earnings", value = "Rp 85K")
                    DashboardStat(label = "Rating", value = "4.8 ⭐")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Incoming order button (demo)
                Button(
                    onClick = onIncomingOrderClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OkePrimary)
                ) {
                    Text(
                        text = "Simulate Incoming Order",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 11.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    OkedriverTheme { DashboardScreen() }
}

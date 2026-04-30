package com.application.okedriver.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.*

/**
 * Premium modernized Dashboard Screen.
 * Featuring glassmorphism overlays, pulsing location indicator, and refined navigation.
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
    var selectedMenuItem by remember { mutableStateOf("Dashboard") }
    var isOnline by remember { mutableStateOf(true) }

    // ── Animations ────────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "marker_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier              = Modifier.width(300.dp),
                drawerContainerColor  = OkeSurface,
                drawerContentColor    = OkeTextPrimary,
                drawerShape           = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                ProfileDrawerHeader(
                    userName   = "Ady Driver",
                    isVerified = true,
                    isOnline   = isOnline
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DrawerSectionLabel("Main Menu")
                    
                    DrawerMenuItem(
                        icon       = Icons.Rounded.Dashboard,
                        label      = "Dashboard",
                        isSelected = selectedMenuItem == "Dashboard",
                        onClick    = {
                            selectedMenuItem = "Dashboard"
                            scope.launch { drawerState.close() }
                        }
                    )
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
                        label      = "Wallet",
                        isSelected = selectedMenuItem == "Wallet",
                        onClick    = {
                            selectedMenuItem = "Wallet"
                            scope.launch { drawerState.close() }
                            onWalletClick()
                        }
                    )
                    
                    DrawerSectionLabel("Settings")
                    
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
                        label      = "Preferences",
                        isSelected = selectedMenuItem == "Preferences",
                        onClick    = {
                            selectedMenuItem = "Preferences"
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuItem(
                        icon       = Icons.AutoMirrored.Rounded.HelpOutline,
                        label      = "Help & Support",
                        onClick    = {}
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    DrawerMenuItem(
                        icon      = Icons.AutoMirrored.Rounded.ExitToApp,
                        label     = "Logout",
                        isDanger  = true,
                        onClick   = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeDarkBg)
        ) {
            // ── Map Background ────────────────────────────────────────────
            OkeDriverMap(modifier = Modifier.fillMaxSize())

            // ── Pulse Indicator (Center) ──────────────────────────────────
            if (isOnline) {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(
                            color = OkePrimary.copy(alpha = pulseAlpha),
                            radius = (size.minDimension / 4f) * pulseScale
                        )
                    }
                }
            }

            // ── Floating Header ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { scope.launch { drawerState.open() } },
                    color = OkeDarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                }

                // Balance Pill
                Surface(
                    modifier = Modifier
                        .clip(OkeShapeChip)
                        .clickable { onWalletClick() },
                    color = OkeDarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = OkePrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Rp 300.000",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Avatar
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() },
                    color = OkePrimary,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            }

            // ── Premium Floating Bottom Panel ─────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .clip(OkeShapeSheet)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(OkeDarkSurface.copy(alpha = 0.95f), OkeDarkBg)
                        )
                    )
                    .border(
                        1.dp, 
                        Color.White.copy(alpha = 0.12f), 
                        OkeShapeSheet
                    )
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                // Online Switch Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isOnline) "You're Online" else "You're Offline",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isOnline) "Actively searching for orders" else "Go online to start earning",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { 
                            isOnline = it
                            if (it) onIncomingOrderClick()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OkePrimary,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.scale(1.1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stats Dashboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DashboardStatItem(
                        icon = Icons.Rounded.Route,
                        label = "Orders",
                        value = "12",
                        tint = OkePrimaryLight
                    )
                    DashboardStatItem(
                        icon = Icons.Rounded.Payments,
                        label = "Earnings",
                        value = "Rp 85k",
                        tint = OkeSuccess
                    )
                    DashboardStatItem(
                        icon = Icons.Rounded.Star,
                        label = "Rating",
                        value = "4.9",
                        tint = OkeWarning
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    OkedriverTheme { DashboardScreen() }
}

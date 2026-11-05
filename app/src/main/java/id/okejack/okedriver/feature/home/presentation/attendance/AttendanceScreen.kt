package id.okejack.okedriver.feature.home.presentation.attendance

import id.okejack.okedriver.core.designsystem.component.DriverButton as Button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import id.okejack.okedriver.core.service.LocationHelper
import id.okejack.okedriver.core.designsystem.component.LocalLocationConsent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBackClick: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    // Update jam setiap detik
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkStatus()
        while (true) {
            delay(60_000)
            viewModel.checkStatus()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Auto back setelah absen berhasil
    var justSubmitted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationConsent = LocalLocationConsent.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.submitAttendance(onSuccess = { justSubmitted = true })
        }
    }
    LaunchedEffect(state.hasAttended, justSubmitted) {
        if (state.hasAttended && justSubmitted) {
            delay(2000)
            onBackClick()
        }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (state.hasAttended) OkeSuccess else OkePrimary,
        animationSpec = tween(600),
        label = "buttonColor"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (state.isSubmitting) 0.95f else 1f,
        animationSpec = tween(150),
        label = "buttonScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Absensi Driver", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeBg)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = OkePrimary)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header tanggal & jam
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OkeShapeCard,
                        colors = CardDefaults.cardColors(containerColor = OkeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OkeTextSecondary
                            )
                            Text(
                                text = currentTime,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = OkeTextPrimary
                            )
                        }
                    }

                    // Status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OkeShapeCard,
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.hasAttended) OkeSuccess.copy(alpha = 0.08f)
                                            else OkeWarning.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (state.hasAttended) Icons.Rounded.CheckCircle
                                              else Icons.Rounded.HowToReg,
                                contentDescription = null,
                                tint = if (state.hasAttended) OkeSuccess else OkeWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = if (state.hasAttended) "Absensi hari ini sudah tercatat"
                                           else "Belum absen hari ini",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.hasAttended) OkeSuccess else OkeWarning
                                )
                                Text(
                                    text = if (state.hasAttended) "Anda sudah bisa daftar antrean bandara"
                                           else "Tekan tombol ABSEN di bawah untuk mencatat kehadiran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OkeTextSecondary
                                )
                            }
                        }
                    }

                    QueueStatusCard(
                        state = state,
                        onRefresh = { viewModel.checkStatus() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tombol ABSEN besar
                    Box(
                        modifier = Modifier
                            .scale(buttonScale)
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.hasAttended || state.isSubmitting)
                                    Brush.radialGradient(listOf(buttonColor, buttonColor))
                                else
                                    Brush.radialGradient(
                                        listOf(OkeLoginGradientTop, OkePrimaryDark)
                                    )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.hasAttended) Icons.Rounded.CheckCircle
                                                  else Icons.Rounded.HowToReg,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (state.hasAttended) "SELESAI" else "ABSEN",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Tombol tap (overlay invisible di atas circle untuk handle click)
                    if (!state.hasAttended && !state.isSubmitting) {
                        Button(
                            onClick = {
                                locationConsent.request {
                                    if (LocationHelper.hasForegroundLocationPermission(context)) {
                                        viewModel.submitAttendance(onSuccess = { justSubmitted = true })
                                    } else {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = OkeShapeButton,
                            colors = ButtonDefaults.buttonColors(containerColor = OkePrimary)
                        ) {
                            Text(
                                text = "Catat Kehadiran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (state.hasAttended && justSubmitted) {
                        Text(
                            text = if (state.hasQueueNumber) {
                                "Absensi berhasil. Nomor antrean ${state.queueNumber}."
                            } else {
                                "Absensi berhasil! Kembali ke beranda..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = OkeSuccess,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueStatusCard(
    state: AttendanceUiState,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = OkeShapeCard,
        colors = CardDefaults.cardColors(containerColor = OkeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = OkePrimary.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = OkePrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = when {
                        state.hasQueueNumber -> "Nomor antrean ${state.queueNumber}"
                        state.hasAttended -> "Belum masuk antrean"
                        else -> "Antrean belum tersedia"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OkeTextPrimary
                )
                Text(
                    text = queueDescription(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary
                )
            }
            IconButton(onClick = onRefresh, enabled = !state.isLoading && !state.isSubmitting) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Muat ulang antrean")
            }
        }
    }
}

private fun queueDescription(state: AttendanceUiState): String {
    if (!state.hasAttended) return "Absen dulu untuk melihat status antrean bandara"
    if (state.queueMessage.isNotBlank() && !state.hasQueueNumber) return state.queueMessage

    val terminal = state.queueTerminal?.takeIf { it.isNotBlank() } ?: "-"
    val total = state.queueTerminalTotal?.takeIf { it > 0 }
    return if (total != null && state.hasQueueNumber) {
        "$terminal - Total antrean: $total"
    } else {
        terminal
    }
}

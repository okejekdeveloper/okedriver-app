package id.okejack.okedriver.feature.order.presentation.current

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.components.OrderContent
import id.okejack.okedriver.feature.order.domain.model.QrisPaymentPolicy
import id.okejack.okedriver.feature.order.presentation.qris.OrderQrisScreen
import id.okejack.okedriver.core.service.LocationHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.location.Location
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentOrderScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenEditShopping: (Long) -> Unit,
    onOpenPayments: (Long) -> Unit,
    onOrderFinished: (Long) -> Unit,
    viewModel: CurrentOrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var farDistance by remember { mutableStateOf<Float?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedCancelReason by remember {
        mutableStateOf(DriverOrder.currentOrderCancelReasons.first())
    }

    LaunchedEffect(orderId, lifecycleOwner, state.showQris) {
        if (!state.showQris) lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) { viewModel.loadOrder(); delay(15_000) }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadOrder()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state.showQris && state.order != null) {
        Dialog(onDismissRequest = viewModel::closeQris, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            OrderQrisScreen(
                orderId = state.order!!.id,
                onBack = viewModel::closeQris,
                onPaymentComplete = { viewModel.onQrisComplete(onOrderFinished) }
            )
        }
    }

    farDistance?.let { distance ->
        AlertDialog(
            onDismissRequest = { farDistance = null },
            title = { Text("Tampilkan QRIS?") },
            text = { Text("Anda masih ${distance.toInt()} meter dari tujuan. Yakin ingin menampilkan QRIS sekarang?") },
            confirmButton = { TextButton(onClick = { farDistance = null; viewModel.openQris() }) { Text("Tampilkan QRIS") } },
            dismissButton = { TextButton(onClick = { farDistance = null }) { Text("Batal") } }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan order?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih alasan pembatalan order.")
                    DriverOrder.currentOrderCancelReasons.forEach { reason ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCancelReason == reason,
                                onClick = { selectedCancelReason = reason }
                            )
                            Text(
                                text = reason.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelOrder(
                            reasonCode = selectedCancelReason.code,
                            onCancelled = onBackClick
                        )
                    }
                ) {
                    Text("Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Kembali")
                }
            }
        )
    }

    fun requestQris() {
        val order = state.order ?: return
                                    val target = order.destinationLatLng.split(",").map { it.trim().toDoubleOrNull() }
                                    LocationHelper.requestLastKnownLocation(context) { location ->
                                        val distance = if (location != null && target.size == 2 &&
                                            target[0] != null && target[1] != null &&
                                            (target[0] != 0.0 || target[1] != 0.0) &&
                                            (location.latitude != 0.0 || location.longitude != 0.0)
                                        ) {
                                            FloatArray(1).also {
                                                Location.distanceBetween(location.latitude, location.longitude, target[0]!!, target[1]!!, it)
                                            }[0]
                                        } else null
                                        if (QrisPaymentPolicy.isFarFromDestination(distance)) farDistance = distance
                                        else viewModel.openQris()
                                    }
    }
    CurrentOrderContent(
        order = state.order, isLoading = state.isLoading || !state.hasLoaded,
        isUpdating = state.isUpdatingStatus, isCancelling = state.isCancelling,
        errorMessage = state.errorMessage, onBack = onBackClick,
        onRefresh = { viewModel.loadOrder() },
        onUpdate = { viewModel.updateStatus(onFinished = onOrderFinished) },
        onCancel = { showCancelDialog = true },
        onChat = { state.order?.id?.let(onOpenChat) },
        onEdit = { state.order?.id?.let(onOpenEditShopping) },
        onPayments = { state.order?.id?.let(onOpenPayments) },
        onQris = ::requestQris
    )
}

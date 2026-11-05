package id.okejack.okedriver.feature.settings.presentation.bid

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.OkeButton
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverBidScreen(
    onBackClick: () -> Unit,
    viewModel: DriverBidViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        (state.errorMessage ?: state.successMessage)?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferensi Order") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PreferenceSlider(
                title = "Batas nilai order",
                valueText = paymentLabel(state.preferences.preferredPayment),
                selectedIndex = nearestIndex(state.paymentLimits, state.preferences.preferredPayment),
                itemCount = state.paymentLimits.size,
                onIndexChange = viewModel::setPaymentIndex
            )
            PreferenceSlider(
                title = "Batas jarak",
                valueText = distanceLabel(state.preferences.preferredDistance),
                selectedIndex = nearestIndex(state.distanceLimits, state.preferences.preferredDistance),
                itemCount = state.distanceLimits.size,
                onIndexChange = viewModel::setDistanceIndex
            )

            if (state.isRideDriver) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Jenis layanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::selectAllServices) { Text("Pilih semua") }
                    }
                    ServiceOption("Ride", state.preferences.rideEnabled, viewModel::setRide)
                    ServiceOption("Delivery", state.preferences.deliveryEnabled, viewModel::setDelivery)
                    ServiceOption("Shopping", state.preferences.shoppingEnabled, viewModel::setShopping)
                    ServiceOption("Food", state.preferences.foodEnabled, viewModel::setFood)
                }
            }

            OkeButton(
                text = "Simpan",
                onClick = viewModel::submit,
                isLoading = state.isSaving
            )
        }
    }
}

@Composable
private fun PreferenceSlider(
    title: String,
    valueText: String,
    selectedIndex: Int,
    itemCount: Int,
    onIndexChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(valueText, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        if (itemCount > 1) Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { onIndexChange(it.toInt()) },
            valueRange = 0f..(itemCount - 1).toFloat(),
            steps = (itemCount - 2).coerceAtLeast(0)
        )
    }
}

@Composable
private fun ServiceOption(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.weight(1f))
    }
}

private fun paymentLabel(value: Int): String =
    if (value == DriverBidPreferences.NO_LIMIT_PAYMENT) "Tanpa batas"
    else "0 - ${CurrencyFormatter.rupiah(value)}"

private fun distanceLabel(value: Int): String =
    if (value == DriverBidPreferences.NO_LIMIT_DISTANCE) "Tanpa batas" else "0 - $value km"

private fun nearestIndex(values: List<Int>, selected: Int): Int =
    values.indices.minByOrNull { kotlin.math.abs(values[it] - selected) } ?: 0

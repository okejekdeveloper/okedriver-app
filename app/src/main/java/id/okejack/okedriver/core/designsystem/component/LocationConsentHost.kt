package id.okejack.okedriver.core.designsystem.component

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.R
import id.okejack.okedriver.core.service.LocationAccess

class LocationConsentController(val granted: Boolean, val request: (() -> Unit) -> Unit)
val LocalLocationConsent = staticCompositionLocalOf<LocationConsentController> {
    error("LocationConsentHost is required")
}

@Composable
fun LocationConsentHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val consentFlow = remember(context) { LocationAccess.consent(context) }
    val consent by consentFlow.collectAsStateWithLifecycle(initialValue = LocationAccess.hasConsent(context))
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val controller = LocationConsentController(consent) { action ->
        if (LocationAccess.hasConsent(context)) action() else pendingAction = action
    }
    CompositionLocalProvider(LocalLocationConsent provides controller) { content() }
    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null; LocationAccess.revoke(context) },
            title = { Text(stringResource(R.string.location_tracking_warning_title)) },
            text = { Text(stringResource(R.string.location_tracking_warning), modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = {
                val action = pendingAction
                pendingAction = null
                LocationAccess.grant(context)
                action?.invoke()
            }) { Text(stringResource(R.string.action_agree)) } },
            dismissButton = { TextButton(onClick = { pendingAction = null; LocationAccess.revoke(context) }) {
                Text(stringResource(R.string.action_deny))
            } }
        )
    }
}

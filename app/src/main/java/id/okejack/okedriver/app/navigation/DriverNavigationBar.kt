package id.okejack.okedriver.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class DriverTab(val route: String, val label: String, val icon: ImageVector) {
    HOME(Route.Home.path, "Beranda", Icons.Rounded.Home),
    WALLET(Route.Balance.path, "Saldo", Icons.Rounded.AccountBalanceWallet),
    HISTORY(Route.OrderHistory.path, "Riwayat", Icons.Rounded.History),
    ACCOUNT(Route.Profile.path, "Akun", Icons.Rounded.Person)
}

@Composable
fun DriverNavigationBar(selectedRoute: String, onSelect: (DriverTab) -> Unit) {
    if (id.okejack.okedriver.core.designsystem.theme.LocalDriverUiStyle.current ==
        id.okejack.okedriver.core.designsystem.theme.DriverUiStyle.DYNAMIC) {
        id.okejack.okedriver.app.navigation.dynamic.DynamicDriverNavigationBar(selectedRoute, onSelect)
    } else id.okejack.okedriver.app.navigation.simple.SimpleDriverNavigationBar(selectedRoute, onSelect)
}

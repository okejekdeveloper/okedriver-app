package id.okejack.okedriver.feature.wallet.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*

@Composable
fun WalletScreen(
    onBackClick: () -> Unit = {}, onTopUpClick: () -> Unit = {}, onWithdrawClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {}, onWithdrawHistoryClick: () -> Unit = {},
    onTopUpHistoryClick: () -> Unit = {}, onBonusClick: () -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel(), isRoot: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnScreenResume { viewModel.loadBalance() }
    WalletContent(state, if (isRoot) null else onBackClick, viewModel::loadBalance,
        onTopUpClick, onWithdrawClick, onTransactionsClick, onWithdrawHistoryClick, onTopUpHistoryClick, onBonusClick)
}

@Composable
fun WalletContent(
    state: WalletUiState, onBack: (() -> Unit)?, onRefresh: () -> Unit,
    onTopUp: () -> Unit, onWithdraw: () -> Unit, onTransactions: () -> Unit,
    onWithdrawHistory: () -> Unit, onTopUpHistory: () -> Unit, onBonus: () -> Unit
) {
    if (id.okejack.okedriver.core.designsystem.theme.LocalDriverUiStyle.current ==
        id.okejack.okedriver.core.designsystem.theme.DriverUiStyle.DYNAMIC) {
        id.okejack.okedriver.feature.wallet.presentation.dynamic.DynamicWalletContent(state, onBack, onRefresh,
            onTopUp, onWithdraw, onTransactions, onWithdrawHistory, onTopUpHistory, onBonus)
    } else id.okejack.okedriver.feature.wallet.presentation.simple.SimpleWalletContent(state, onBack, onRefresh,
        onTopUp, onWithdraw, onTransactions, onWithdrawHistory, onTopUpHistory, onBonus)
}

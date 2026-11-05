package id.okejack.okedriver.feature.order.presentation.shopping

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.theme.*
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import java.math.BigDecimal

private enum class AddType { ITEM, PARKING, TOLL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingItemsScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    viewModel: EditShoppingItemsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showAddTypeSheet by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }
    var showFeeDialog by remember { mutableStateOf<AddType?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item Belanja", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (!state.isAlreadyEdited && state.isDirty) {
                        TextButton(
                            onClick = { viewModel.submitItems(onSuccess = onBackClick) },
                            enabled = !state.isSubmitting
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = OkePrimary
                                )
                            } else {
                                Text("Simpan", color = OkePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isAlreadyEdited && !state.isLoading) {
                FloatingActionButton(
                    onClick = { showAddTypeSheet = true },
                    containerColor = OkePrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Tambah item", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeBg)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OkePrimary
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Warning banner kalau sudah pernah diedit
                        if (state.isAlreadyEdited) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(OkeWarning.copy(alpha = 0.12f))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = OkeWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Item sudah pernah diedit — tidak bisa diubah lagi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OkeWarning,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Unsaved changes banner
                        if (state.isDirty && !state.isAlreadyEdited) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(OkePrimary.copy(alpha = 0.08f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = OkePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Ada perubahan yang belum disimpan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OkePrimary
                                )
                            }
                        }

                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🛒", fontSize = 48.sp)
                                    Text(
                                        text = "Belum ada item",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OkeTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!state.isAlreadyEdited) {
                                        Text(
                                            text = "Ketuk + untuk menambah item",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OkeTextSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(items = state.items, key = { i, _ -> i }) { index, item ->
                                    ShoppingItemCard(
                                        item = item,
                                        readOnly = state.isAlreadyEdited,
                                        onEdit = {
                                            editingIndex = index
                                            if (item.isFeeType) showFeeDialog = AddType.ITEM
                                            else showItemDialog = true
                                        },
                                        onDelete = { showDeleteConfirm = index }
                                    )
                                }
                            }
                        }

                        // Total
                        if (state.items.isNotEmpty()) {
                            HorizontalDivider(color = OkeDivider)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(OkeSurface)
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total (${state.items.size} item)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OkeTextSecondary
                                )
                                Text(
                                    CurrencyFormatter.rupiah(
                                        state.items.fold(BigDecimal.ZERO) { acc, item -> acc + item.total }
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = OkePrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add type bottom sheet ─────────────────────────────────────────────
    if (showAddTypeSheet) {
        ModalBottomSheet(onDismissRequest = { showAddTypeSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Tambah Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AddTypeOption("Item Belanja", Icons.Rounded.ShoppingCart) {
                    showAddTypeSheet = false
                    editingIndex = null
                    showItemDialog = true
                }
                AddTypeOption("Biaya Parkir", Icons.Rounded.DirectionsCar) {
                    showAddTypeSheet = false
                    editingIndex = null
                    showFeeDialog = AddType.PARKING
                }
                AddTypeOption("Biaya Tol", Icons.Rounded.Route) {
                    showAddTypeSheet = false
                    editingIndex = null
                    showFeeDialog = AddType.TOLL
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ── Item dialog (add or edit) ────────────────────────────────────────
    if (showItemDialog) {
        val editing = editingIndex?.let { state.items.getOrNull(it) }
        ItemFormDialog(
            initial = editing,
            onDismiss = { showItemDialog = false; editingIndex = null },
            onConfirm = { name, detail, qty, price ->
                val idx = editingIndex
                if (idx == null) viewModel.addItem(name, detail, qty, price)
                else viewModel.updateItem(idx, name, detail, qty, price)
                showItemDialog = false
                editingIndex = null
            }
        )
    }

    // ── Fee dialog (parking / toll / edit fee) ───────────────────────────
    if (showFeeDialog != null) {
        val editing = editingIndex?.let { state.items.getOrNull(it) }
        val feeLabel = when (showFeeDialog) {
            AddType.PARKING -> "Biaya Parkir"
            AddType.TOLL -> "Biaya Tol"
            else -> editing?.name ?: "Biaya"
        }
        FeeFormDialog(
            label = feeLabel,
            initialPrice = editing?.price ?: BigDecimal.ZERO,
            onDismiss = { showFeeDialog = null; editingIndex = null },
            onConfirm = { price ->
                val idx = editingIndex
                if (idx == null) viewModel.addFeeItem(feeLabel, price)
                else viewModel.updateFeeItem(idx, price)
                showFeeDialog = null
                editingIndex = null
            }
        )
    }

    // ── Delete confirm dialog ─────────────────────────────────────────────
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus item?") },
            text = {
                val name = state.items.getOrNull(showDeleteConfirm!!)?.name ?: "Item"
                Text("\"$name\" akan dihapus dari daftar belanja.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm?.let { viewModel.removeItem(it) }
                    showDeleteConfirm = null
                }) { Text("Hapus", color = OkeDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ShoppingItemCard(
    item: EditableItem,
    readOnly: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (item.isFeeType) OkeWarning.copy(alpha = 0.1f) else OkePrimaryContainer,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.isFeeType) Icons.Rounded.Receipt else Icons.Rounded.ShoppingCart,
                    contentDescription = null,
                    tint = if (item.isFeeType) OkeWarning else OkePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.ifBlank { "Item" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OkeTextPrimary
                )
                if (item.detail.isNotBlank()) {
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextSecondary
                    )
                }
                Text(
                    text = if (item.isFeeType)
                        CurrencyFormatter.rupiah(item.price)
                    else
                        "${item.quantity}× ${CurrencyFormatter.rupiah(item.price)} = ${CurrencyFormatter.rupiah(item.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (!readOnly) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = OkePrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = OkeDanger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddTypeOption(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = OkePrimary)
            Text(label, color = OkeTextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ItemFormDialog(
    initial: EditableItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, BigDecimal) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var detail by remember { mutableStateOf(initial?.detail ?: "") }
    var qty by remember { mutableIntStateOf(initial?.quantity ?: 1) }
    var price by remember { mutableStateOf(initial?.price?.toPlainString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Item" else "Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama item") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Keterangan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Jumlah:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(60.dp))
                    IconButton(
                        onClick = { if (qty > 1) qty-- },
                        modifier = Modifier.size(32.dp)
                    ) { Icon(Icons.Rounded.Remove, contentDescription = "Kurang", tint = OkePrimary) }
                    Text(
                        "$qty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(30.dp)
                    )
                    IconButton(
                        onClick = { qty++ },
                        modifier = Modifier.size(32.dp)
                    ) { Icon(Icons.Rounded.Add, contentDescription = "Tambah", tint = OkePrimary) }
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Harga (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = price.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (name.isNotBlank() && qty >= 1)
                        onConfirm(name.trim(), detail.trim(), qty, p)
                }
            ) { Text("Simpan", color = OkePrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun FeeFormDialog(
    label: String,
    initialPrice: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit
) {
    var price by remember {
        mutableStateOf(if (initialPrice > BigDecimal.ZERO) initialPrice.toPlainString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Nominal (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = price.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (p > BigDecimal.ZERO) onConfirm(p)
                }
            ) { Text("Simpan", color = OkePrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

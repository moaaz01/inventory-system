package com.inventory.system.presentation.warehouses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.system.presentation.components.EmptyScreen
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehousesScreen(
    onWarehouseClick: (Int) -> Unit,
    onAddWarehouse: (() -> Unit)? = null,
    viewModel: WarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المستودعات") },
                actions = {
                    IconButton(onClick = viewModel::loadWarehouses) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        },
        floatingActionButton = {
            onAddWarehouse?.let {
                FloatingActionButton(onClick = it) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مستودع")
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadWarehouses, modifier = Modifier.padding(padding))
            uiState.warehouses.isEmpty() -> EmptyScreen("لا توجد مستودعات", modifier = Modifier.padding(padding))
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.warehouses) { warehouse ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onWarehouseClick(warehouse.id) }
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warehouse, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(warehouse.name, style = MaterialTheme.typography.titleSmall)
                                warehouse.location?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                if (warehouse.stockInfo.isNotEmpty()) {
                                    Text(
                                        "${warehouse.stockInfo.size} منتج · ${warehouse.stockInfo.sumOf { it.quantity }} قطعة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseDetailScreen(
    warehouseId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: ((Int) -> Unit)? = null,
    viewModel: WarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(warehouseId) { viewModel.loadWarehouse(warehouseId) }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            viewModel.clearActionState()
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionState()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف هذا المستودع؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteWarehouse(warehouseId)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل المستودع") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    onNavigateToEdit?.let { nav ->
                        IconButton(onClick = { nav(warehouseId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل")
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = { viewModel.loadWarehouse(warehouseId) }, modifier = Modifier.padding(padding))
            uiState.selectedWarehouse != null -> {
                val w = uiState.selectedWarehouse!!
                val totalQuantity = w.stockInfo.sumOf { it.quantity }
                val lowStockThreshold = 10
                LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(w.name, style = MaterialTheme.typography.titleLarge)
                                w.location?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        val lowCount = w.stockInfo.count { it.quantity < lowStockThreshold }
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${w.stockInfo.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("منتج", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$totalQuantity", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("إجمالي الكميات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                if (lowCount > 0) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$lowCount", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                                        Text("مخزون منخفض", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("المنتجات والكميات", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (w.stockInfo.isEmpty()) {
                        item { Text("لا توجد منتجات في هذا المستودع", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(w.stockInfo) { stock ->
                            val isLow = stock.quantity < lowStockThreshold
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLow) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                                     else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isLow) {
                                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(stock.productName ?: "منتج #${stock.productId}", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text(
                                        "${stock.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

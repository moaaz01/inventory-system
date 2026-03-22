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
    viewModel: WarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(warehouseId) { viewModel.loadWarehouse(warehouseId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل المستودع") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = { viewModel.loadWarehouse(warehouseId) }, modifier = Modifier.padding(padding))
            uiState.selectedWarehouse != null -> {
                val w = uiState.selectedWarehouse!!
                LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(w.name, style = MaterialTheme.typography.titleLarge)
                                w.location?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stock.productName ?: "منتج #${stock.productId}")
                                    Text("${stock.quantity}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

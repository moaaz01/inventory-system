package com.inventory.system.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.inventory.system.domain.model.InventoryReportItem
import com.inventory.system.domain.model.Movement
import com.inventory.system.domain.model.Product
import com.inventory.system.presentation.components.EmptyScreen
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("المخزون", "مخزون منخفض", "الحركات")
    val uiState by viewModel.uiState.collectAsState()
    val movements = viewModel.movements.collectAsLazyPagingItems()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("التقارير") })
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(selected = selectedTab == index, onClick = {
                    selectedTab = index
                    when (index) {
                        0 -> viewModel.loadInventoryReport()
                        1 -> viewModel.loadLowStockReport()
                    }
                }, text = { Text(tab) })
            }
        }

        when (selectedTab) {
            0 -> {
                LaunchedEffect(Unit) { viewModel.loadInventoryReport() }
                InventoryReportTab(uiState, onRetry = viewModel::loadInventoryReport)
            }
            1 -> {
                LaunchedEffect(Unit) { viewModel.loadLowStockReport() }
                LowStockTab(uiState, onRetry = viewModel::loadLowStockReport)
            }
            2 -> MovementsTab(movements)
        }
    }
}

@Composable
fun InventoryReportTab(uiState: ReportsUiState, onRetry: () -> Unit) {
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = onRetry)
        uiState.inventoryReport.isEmpty() -> EmptyScreen("لا توجد بيانات")
        else -> LazyColumn(Modifier.fillMaxSize()) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("المنتج", Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                    Text("الكمية", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    Text("الحد الأدنى", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                }
                HorizontalDivider()
            }
            items(uiState.inventoryReport) { item -> InventoryReportRow(item) }
        }
    }
}

@Composable
fun InventoryReportRow(item: InventoryReportItem) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(2f)) {
            Text(item.productName, style = MaterialTheme.typography.bodyMedium)
            Text(item.sku, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "${item.totalQuantity}",
            Modifier.weight(1f),
            color = if (item.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text("${item.minStockLevel}")
            if (item.isLowStock) Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
}

@Composable
fun LowStockTab(uiState: ReportsUiState, onRetry: () -> Unit) {
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = onRetry)
        uiState.lowStockProducts.isEmpty() -> EmptyScreen("لا توجد منتجات بمخزون منخفض 🎉")
        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(uiState.lowStockProducts) { product -> LowStockProductCard(product) }
        }
    }
}

@Composable
fun LowStockProductCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text(product.sku, style = MaterialTheme.typography.bodySmall)
            }
            Text("الحد: ${product.minStockLevel}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MovementsTab(movements: androidx.paging.compose.LazyPagingItems<Movement>) {
    when (movements.loadState.refresh) {
        is LoadState.Loading -> LoadingScreen()
        is LoadState.Error -> {
            val error = (movements.loadState.refresh as LoadState.Error).error
            ErrorScreen(error.message ?: "خطأ", onRetry = { movements.retry() })
        }
        else -> {
            if (movements.itemCount == 0) {
                EmptyScreen("لا توجد حركات")
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(movements.itemCount) { index ->
                        movements[index]?.let { MovementCard(it) }
                    }
                    when (movements.loadState.append) {
                        is LoadState.Loading -> item { Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) { CircularProgressIndicator() } }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MovementCard(movement: Movement) {
    val typeLabel = when (movement.type) {
        "receipt" -> "استلام"
        "issue" -> "صرف"
        "transfer" -> "نقل"
        else -> movement.type
    }
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(typeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(movement.createdAt.take(10), style = MaterialTheme.typography.bodySmall)
            }
            Text(movement.productName ?: "منتج #${movement.productId}", style = MaterialTheme.typography.bodyMedium)
            Text("الكمية: ${movement.quantity}", style = MaterialTheme.typography.bodySmall)
            movement.referenceNumber?.let { Text("مرجع: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

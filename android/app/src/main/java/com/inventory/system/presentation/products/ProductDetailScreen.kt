package com.inventory.system.presentation.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل المنتج") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(productId) }) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = { viewModel.loadProduct(productId) }, modifier = Modifier.padding(padding))
            uiState.selectedProduct != null -> {
                val p = uiState.selectedProduct!!
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow("اسم المنتج", p.name)
                            InfoRow("رمز المنتج (SKU)", p.sku)
                            p.categoryName?.let { InfoRow("الفئة", it) }
                            p.unitName?.let { InfoRow("الوحدة", it) }
                            InfoRow("الحد الأدنى للمخزون", p.minStockLevel.toString())
                        }
                    }

                    if (p.stockInfo.isNotEmpty()) {
                        Text("المخزون حسب المستودع", style = MaterialTheme.typography.titleMedium)
                        p.stockInfo.forEach { stock ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stock.warehouseName ?: "مستودع #${stock.warehouseId}")
                                    Text(
                                        "${stock.quantity}",
                                        color = if (stock.quantity <= p.minStockLevel) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    } else {
                        Text("لا يوجد مخزون لهذا المنتج", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

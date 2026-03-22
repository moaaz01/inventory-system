package com.inventory.system.presentation.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.inventory.system.domain.model.Product
import com.inventory.system.presentation.components.EmptyScreen
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onProductClick: (Int) -> Unit,
    onAddProduct: () -> Unit,
    onBarcodeScanner: (() -> Unit)? = null,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val products = viewModel.products.collectAsLazyPagingItems()
    var showFilterMenu by remember { mutableStateOf(false) }

    // Refresh products when screen resumes (e.g. after adding a product)
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstResume by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                } else {
                    viewModel.refreshProducts()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المنتجات") },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, null)
                    }
                    onBarcodeScanner?.let { scanner ->
                        IconButton(onClick = scanner) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح باركود")
                        }
                    }
                    IconButton(onClick = onAddProduct) {
                        Icon(Icons.Default.Add, null)
                    }

                    DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                        DropdownMenuItem(text = { Text("الكل") }, onClick = {
                            viewModel.setCategory(null); showFilterMenu = false
                        })
                        uiState.categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                                viewModel.setCategory(cat.id); showFilterMenu = false
                            })
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("بحث عن منتج...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            when (products.loadState.refresh) {
                is LoadState.Loading -> LoadingScreen()
                is LoadState.Error -> {
                    val error = (products.loadState.refresh as LoadState.Error).error
                    ErrorScreen(message = error.message ?: "خطأ في التحميل", onRetry = { products.retry() })
                }
                else -> {
                    if (products.itemCount == 0 && products.loadState.refresh !is LoadState.Loading) {
                        EmptyScreen(
                            message = "لا توجد منتجات",
                            actionLabel = "إضافة منتج",
                            onAction = onAddProduct
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(products.itemCount) { index ->
                                products[index]?.let { product ->
                                    ProductListItem(product = product, onClick = { onProductClick(product.id) })
                                }
                            }
                            when (products.loadState.append) {
                                is LoadState.Loading -> item { Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) { CircularProgressIndicator() } }
                                is LoadState.Error -> item {
                                    val error = (products.loadState.append as LoadState.Error).error
                                    Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) {
                                        TextButton(onClick = { products.retry() }) { Text("إعادة المحاولة") }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListItem(product: Product, onClick: () -> Unit) {
    val totalStock = product.stockInfo.sumOf { it.quantity }
    val isLow = totalStock <= product.minStockLevel
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                product.categoryName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(
                    "الحد الأدنى: ${product.minStockLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalStock",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (isLow) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("منخفض", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

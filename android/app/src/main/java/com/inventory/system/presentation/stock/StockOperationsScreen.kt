package com.inventory.system.presentation.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.system.domain.model.Product
import com.inventory.system.domain.model.Warehouse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOperationsScreen(viewModel: StockViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("استلام", "صرف", "نقل")

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index; viewModel.clearMessages() }, text = { Text(tab) })
            }
        }

        uiState.error?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) { Text(err, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
        }

        uiState.success?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) { Text(msg, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer) }
        }

        when (selectedTab) {
            0 -> ReceiptTab(uiState, onSubmit = { pid, wid, qty, ref, notes -> viewModel.receipt(pid, wid, qty, ref, notes) })
            1 -> IssueTab(uiState, onSubmit = { pid, wid, qty, ref -> viewModel.issue(pid, wid, qty, ref) })
            2 -> TransferTab(uiState, onSubmit = { pid, fid, tid, qty -> viewModel.transfer(pid, fid, tid, qty) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptTab(uiState: StockUiState, onSubmit: (Int, Int, Int, String?, String?) -> Unit) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var quantity by remember { mutableStateOf("") }
    var refNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }
    var warehouseExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("استلام مخزون", style = MaterialTheme.typography.titleMedium)

        ProductDropdown(products = uiState.products, selected = selectedProduct, expanded = productExpanded,
            onExpandedChange = { productExpanded = it }, onSelect = { selectedProduct = it; productExpanded = false })

        WarehouseDropdown(warehouses = uiState.warehouses, selected = selectedWarehouse, expanded = warehouseExpanded,
            onExpandedChange = { warehouseExpanded = it }, onSelect = { selectedWarehouse = it; warehouseExpanded = false })

        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        OutlinedTextField(value = refNumber, onValueChange = { refNumber = it }, label = { Text("رقم المرجع") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") },
            modifier = Modifier.fillMaxWidth(), minLines = 2)

        Button(
            onClick = { onSubmit(selectedProduct!!.id, selectedWarehouse!!.id, quantity.toInt(), refNumber.ifBlank { null }, notes.ifBlank { null }) },
            enabled = !uiState.isLoading && selectedProduct != null && selectedWarehouse != null && quantity.toIntOrNull() != null && quantity.toInt() > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("تأكيد الاستلام")
        }
    }
}

@Composable
fun IssueTab(uiState: StockUiState, onSubmit: (Int, Int, Int, String?) -> Unit) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var quantity by remember { mutableStateOf("") }
    var refNumber by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }
    var warehouseExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("صرف مخزون", style = MaterialTheme.typography.titleMedium)

        ProductDropdown(products = uiState.products, selected = selectedProduct, expanded = productExpanded,
            onExpandedChange = { productExpanded = it }, onSelect = { selectedProduct = it; productExpanded = false })

        WarehouseDropdown(warehouses = uiState.warehouses, selected = selectedWarehouse, expanded = warehouseExpanded,
            onExpandedChange = { warehouseExpanded = it }, onSelect = { selectedWarehouse = it; warehouseExpanded = false })

        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        OutlinedTextField(value = refNumber, onValueChange = { refNumber = it }, label = { Text("رقم المرجع") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)

        Button(
            onClick = { onSubmit(selectedProduct!!.id, selectedWarehouse!!.id, quantity.toInt(), refNumber.ifBlank { null }) },
            enabled = !uiState.isLoading && selectedProduct != null && selectedWarehouse != null && quantity.toIntOrNull() != null && quantity.toInt() > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("تأكيد الصرف")
        }
    }
}

@Composable
fun TransferTab(uiState: StockUiState, onSubmit: (Int, Int, Int, Int) -> Unit) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var fromWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var toWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var quantity by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("نقل بين المستودعات", style = MaterialTheme.typography.titleMedium)

        ProductDropdown(products = uiState.products, selected = selectedProduct, expanded = productExpanded,
            onExpandedChange = { productExpanded = it }, onSelect = { selectedProduct = it; productExpanded = false })

        WarehouseDropdown(warehouses = uiState.warehouses, selected = fromWarehouse, expanded = fromExpanded, label = "من المستودع",
            onExpandedChange = { fromExpanded = it }, onSelect = { fromWarehouse = it; fromExpanded = false })

        WarehouseDropdown(warehouses = uiState.warehouses, selected = toWarehouse, expanded = toExpanded, label = "إلى المستودع",
            onExpandedChange = { toExpanded = it }, onSelect = { toWarehouse = it; toExpanded = false })

        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Button(
            onClick = { onSubmit(selectedProduct!!.id, fromWarehouse!!.id, toWarehouse!!.id, quantity.toInt()) },
            enabled = !uiState.isLoading && selectedProduct != null && fromWarehouse != null && toWarehouse != null && fromWarehouse != toWarehouse && quantity.toIntOrNull() != null && quantity.toInt() > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("تأكيد النقل")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDropdown(
    products: List<Product>,
    selected: Product?,
    expanded: Boolean,
    label: String = "المنتج",
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf(selected?.name ?: "") }
    var filteredProducts by remember { mutableStateOf(products) }

    LaunchedEffect(products) {
        filteredProducts = if (searchQuery.isBlank()) products
        else products.filter { p -> p.name.contains(searchQuery, ignoreCase = true) || p.sku.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(selected) {
        if (selected != null) searchQuery = selected.name
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                filteredProducts = if (query.isBlank()) products
                else products.filter { p -> p.name.contains(query, ignoreCase = true) || p.sku.contains(query, ignoreCase = true) }
                onExpandedChange(true)
            },
            readOnly = false,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            val displayProducts = if (searchQuery.isBlank()) products.take(50) else filteredProducts.take(50)
            displayProducts.forEach { product ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.bodyMedium)
                            Text(product.sku, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = {
                        searchQuery = product.name
                        onSelect(product)
                    }
                )
            }
            if (displayProducts.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("لا توجد نتائج") },
                    onClick = { onExpandedChange(false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseDropdown(
    warehouses: List<Warehouse>,
    selected: Warehouse?,
    expanded: Boolean,
    label: String = "المستودع",
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Warehouse) -> Unit
) {
    var searchQuery by remember { mutableStateOf(selected?.name ?: "") }
    var filteredWarehouses by remember { mutableStateOf(warehouses) }

    LaunchedEffect(warehouses) {
        filteredWarehouses = if (searchQuery.isBlank()) warehouses
        else warehouses.filter { w -> w.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(selected) {
        if (selected != null) searchQuery = selected.name
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                filteredWarehouses = if (query.isBlank()) warehouses
                else warehouses.filter { w -> w.name.contains(query, ignoreCase = true) }
                onExpandedChange(true)
            },
            readOnly = false,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            val displayWarehouses = if (searchQuery.isBlank()) warehouses.take(50) else filteredWarehouses.take(50)
            displayWarehouses.forEach { warehouse ->
                DropdownMenuItem(
                    text = { Text(warehouse.name, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        searchQuery = warehouse.name
                        onSelect(warehouse)
                    }
                )
            }
            if (displayWarehouses.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("لا توجد نتائج") },
                    onClick = { onExpandedChange(false) }
                )
            }
        }
    }
}

package com.inventory.system.presentation.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = productId != null

    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("0") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedUnitId by remember { mutableStateOf<Int?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        }
    }

    LaunchedEffect(uiState.selectedProduct) {
        uiState.selectedProduct?.let { p ->
            if (isEdit) {
                sku = p.sku
                name = p.name
                minStock = p.minStockLevel.toString()
                selectedCategoryId = p.categoryId
                selectedUnitId = p.unitId
            }
        }
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            viewModel.clearActionState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "تعديل منتج" else "إضافة منتج") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = sku, onValueChange = { sku = it },
                label = { Text("رمز المنتج (SKU)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("اسم المنتج") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = minStock, onValueChange = { minStock = it },
                label = { Text("الحد الأدنى للمخزون") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            // Category picker
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = uiState.categories.find { it.id == selectedCategoryId }?.name ?: "اختر الفئة",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("الفئة") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    DropdownMenuItem(text = { Text("بدون فئة") }, onClick = { selectedCategoryId = null; categoryExpanded = false })
                    uiState.categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; categoryExpanded = false })
                    }
                }
            }

            // Unit picker
            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                OutlinedTextField(
                    value = uiState.units.find { it.id == selectedUnitId }?.name ?: "اختر الوحدة",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("الوحدة") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    DropdownMenuItem(text = { Text("بدون وحدة") }, onClick = { selectedUnitId = null; unitExpanded = false })
                    uiState.units.forEach { unit ->
                        DropdownMenuItem(text = { Text("${unit.name} (${unit.symbol})") }, onClick = { selectedUnitId = unit.id; unitExpanded = false })
                    }
                }
            }

            uiState.actionError?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(err, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Button(
                onClick = {
                    val min = minStock.toIntOrNull() ?: 0
                    if (isEdit && productId != null) {
                        viewModel.updateProduct(productId, sku, name, selectedCategoryId, selectedUnitId, min)
                    } else {
                        viewModel.createProduct(sku, name, selectedCategoryId, selectedUnitId, min)
                    }
                },
                enabled = !uiState.isLoading && sku.isNotBlank() && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (isEdit) "حفظ التعديلات" else "إضافة المنتج")
            }
        }
    }
}

package com.inventory.system.presentation.products

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.inventory.system.domain.model.Unit as InventoryUnit
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.io.File
import java.io.FileOutputStream

fun generateBarcodeImage(sku: String, width: Int = 400, height: Int = 150): Bitmap? {
    return try {
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(sku, BarcodeFormat.CODE_128, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Int? = null,
    initialSku: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = productId != null
    val context = LocalContext.current

    var sku by remember { mutableStateOf(initialSku ?: "") }
    var name by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("0") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedUnitId by remember { mutableStateOf<Int?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    // Pricing fields
    var retailPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var currencyExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "SYP", "TRY")

    // Barcode dialog state
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Sync generated SKU from ViewModel
    LaunchedEffect(uiState.generatedSku) {
        uiState.generatedSku?.let { sku = it }
    }

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
                retailPrice = p.retailPrice?.toString() ?: ""
                wholesalePrice = p.wholesalePrice?.toString() ?: ""
                selectedCurrency = p.currency
            }
        }
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            viewModel.clearActionState()
            onNavigateBack()
        }
    }

    // Barcode dialog
    if (showBarcodeDialog) {
        if (barcodeBitmap == null && sku.isNotBlank()) {
            barcodeBitmap = generateBarcodeImage(sku)
        }
        barcodeBitmap?.let { bmp ->
            AlertDialog(
                onDismissRequest = { showBarcodeDialog = false; barcodeBitmap = null },
                title = { Text("باركود المنتج: $sku") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "باركود",
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Save barcode image
                        try {
                            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            val file = File(dir, "barcode_${sku}.png")
                            FileOutputStream(file).use { out ->
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                        } catch (_: Exception) {}
                        showBarcodeDialog = false
                        barcodeBitmap = null
                    }) { Text("حفظ") }
                },
                dismissButton = {
                    TextButton(onClick = { showBarcodeDialog = false; barcodeBitmap = null }) { Text("إغلاق") }
                }
            )
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
            // SKU field: readOnly in edit mode, auto-generate button in add mode
            OutlinedTextField(
                value = sku,
                onValueChange = { if (!isEdit && uiState.generatedSku == null) sku = it },
                label = { Text("رمز المنتج (SKU)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = isEdit || uiState.generatedSku != null,
                trailingIcon = {
                    if (!isEdit) {
                        if (uiState.generatedSku != null) {
                            IconButton(onClick = { viewModel.clearActionState(); sku = "" }) {
                                Icon(Icons.Default.Clear, "مسح SKU")
                            }
                        } else {
                            IconButton(onClick = { viewModel.generateNextSku(selectedCategoryId) }) {
                                Icon(Icons.Default.AutoAwesome, "توليد تلقائي")
                            }
                        }
                    }
                }
            )

            // Barcode generate button (shown when SKU is available)
            if (sku.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        barcodeBitmap = null
                        showBarcodeDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("إنشاء باركود 📷")
                }
            }

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

            HorizontalDivider()
            Text("التسعيرة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = retailPrice,
                onValueChange = { retailPrice = it },
                label = { Text("سعر التجزئة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Sell, null) }
            )

            OutlinedTextField(
                value = wholesalePrice,
                onValueChange = { wholesalePrice = it },
                label = { Text("سعر الجملة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.LocalOffer, null) }
            )

            // Currency picker
            ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("العملة") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                    currencies.forEach { cur ->
                        DropdownMenuItem(text = { Text(cur) }, onClick = { selectedCurrency = cur; currencyExpanded = false })
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
                    val rPrice = retailPrice.toDoubleOrNull()
                    val wPrice = wholesalePrice.toDoubleOrNull()
                    if (isEdit && productId != null) {
                        viewModel.updateProduct(productId, sku, name, selectedCategoryId, selectedUnitId, min, rPrice, wPrice, selectedCurrency)
                    } else {
                        viewModel.createProduct(sku, name, selectedCategoryId, selectedUnitId, min, rPrice, wPrice, selectedCurrency)
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


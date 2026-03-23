package com.inventory.system.presentation.pos

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.integration.android.IntentIntegrator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (Int) -> Unit,
    onNavigateToInvoiceHistory: () -> Unit,
    viewModel: CashierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscountDialog by remember { mutableStateOf(false) }
    var manualInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Navigate to invoice detail after creation
    LaunchedEffect(uiState.lastCreatedInvoiceId) {
        uiState.lastCreatedInvoiceId?.let { id ->
            onNavigateToInvoice(id)
        }
    }

    // ZXing launcher
    val zxingLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = com.google.zxing.integration.android.IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            scanResult?.contents?.let { code ->
                viewModel.addBySku(code)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val activity = context as? Activity ?: return@rememberLauncherForActivityResult
            val integrator = IntentIntegrator(activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("مسح الباركود")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(true)
            zxingLauncher.launch(integrator.createScanIntent())
        }
    }

    fun launchScanner() {
        val activity = context as? Activity ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val integrator = IntentIntegrator(activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("وجّه الكاميرا نحو الباركود")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(true)
            zxingLauncher.launch(integrator.createScanIntent())
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الكاشير 🛒") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = onNavigateToInvoiceHistory) {
                        Icon(Icons.Default.History, "سجل الفواتير")
                    }
                    if (uiState.cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, "مسح السلة")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scanner section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { launchScanner() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null)
                        Spacer(Modifier.width(8.dp))
                        Text("📷 مسح باركود")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            label = { Text("باركود / رمز المنتج") },
                            placeholder = { Text("مسح بجهاز خارجي أو كتابة يدوياً") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (manualInput.isNotBlank()) {
                                    viewModel.addBySku(manualInput.trim())
                                    manualInput = ""
                                }
                            })
                        )
                        IconButton(
                            onClick = {
                                if (manualInput.isNotBlank()) {
                                    viewModel.addBySku(manualInput.trim())
                                    manualInput = ""
                                }
                            },
                            enabled = manualInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.AddShoppingCart, "إضافة", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }
            }

            // Cart items
            if (uiState.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("السلة فارغة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("امسح باركود أو أدخل رمز المنتج", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.cartItems, key = { "${it.productId}_${it.sku}" }) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { qty -> viewModel.updateQuantity(item, qty) },
                            onRemove = { viewModel.removeFromCart(item) }
                        )
                    }
                }
            }

            // Loading
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المجموع الفرعي")
                        Text("%.2f".format(uiState.subtotal))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الخصم")
                        Text("-%.2f".format(uiState.discount))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإجمالي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("%.2f".format(uiState.total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Customer name field
                    OutlinedTextField(
                        value = uiState.customerName,
                        onValueChange = { viewModel.updateCustomerName(it) },
                        label = { Text("اسم العميل (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onNavigateToInvoiceHistory,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("الفواتير")
                        }
                        OutlinedButton(
                            onClick = { showDiscountDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("خصم")
                        }
                        Button(
                            onClick = { viewModel.createInvoice() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.cartItems.isNotEmpty() && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            Icon(Icons.Default.Receipt, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إنشاء الفاتورة")
                        }
                    }
                }
            }
        }

        // Discount dialog
        if (showDiscountDialog) {
            var discountInput by remember { mutableStateOf(if (uiState.discount > 0) uiState.discount.toString() else "") }
            AlertDialog(
                onDismissRequest = { showDiscountDialog = false },
                title = { Text("تطبيق خصم") },
                text = {
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("قيمة الخصم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.applyDiscount(discountInput.toDoubleOrNull() ?: 0.0)
                        showDiscountDialog = false
                    }) { Text("تطبيق") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscountDialog = false }) { Text("إلغاء") }
                }
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemDisplay,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("SKU: ${item.sku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.2f × %.0f".format(item.unitPrice, item.quantity), style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Qty controls
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                }
                Text("%.0f".format(item.quantity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("%.2f".format(item.totalPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

package com.inventory.system.presentation.barcode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BarcodeViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current
    var manualInput by remember { mutableStateOf("") }
    var showCameraHint by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        // Show camera hint since it's not available
        showCameraHint = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بحث بالباركود / SKU") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {
                        manualInput = ""
                        viewModel.clearSearch()
                    }) {
                        Icon(Icons.Default.Clear, "مسح البحث")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                Icons.Default.QrCodeScanner,
                null,
                modifier = Modifier.size(72.dp).padding(vertical = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "ابحث عن المنتج بالباركود أو رمز SKU",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Camera hint
            if (showCameraHint) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "📷 مسح بالكاميرا غير متاح حالياً",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "يبني APK على جهاز متصل بالإنترنت لتفعيل الميزة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Search input - supports external barcode scanner (keyboard wedge)
            OutlinedTextField(
                value = manualInput,
                onValueChange = { newValue ->
                    manualInput = newValue
                    // Auto-search when external scanner sends data (ends with Enter)
                    if (newValue.length >= 3) {
                        viewModel.searchBySku(newValue)
                    }
                },
                label = { Text("رمز المنتج أو الباركود (SKU)") },
                placeholder = { Text("مثال: ELEC-001 أو 0123456789012") },
                leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (manualInput.isNotBlank()) {
                            viewModel.searchBySku(manualInput)
                            focusManager.clearFocus()
                        }
                    },
                    onDone = {
                        // External barcode scanners send Enter (Done) after scanning
                        if (manualInput.isNotBlank()) {
                            viewModel.searchBySku(manualInput)
                            focusManager.clearFocus()
                        }
                    }
                ),
                trailingIcon = {
                    if (manualInput.isNotEmpty()) {
                        IconButton(onClick = {
                            manualInput = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, "مسح")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    if (manualInput.isNotBlank()) {
                        viewModel.searchBySku(manualInput)
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                enabled = manualInput.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جاري البحث...")
                } else {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("بحث")
                }
            }

            Spacer(Modifier.height(20.dp))

            // Results
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.foundProduct != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(
                                        uiState.foundProduct!!.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "SKU: ${uiState.foundProduct!!.sku}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    uiState.foundProduct!!.categoryName?.let {
                                        Text(
                                            "الفئة: $it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("الحد الأدنى", style = MaterialTheme.typography.labelSmall)
                                    Text("${uiState.foundProduct!!.minStockLevel}", style = MaterialTheme.typography.bodyLarge)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الكمية المتوفرة", style = MaterialTheme.typography.labelSmall)
                                    val totalQty = uiState.foundProduct!!.stockInfo.sumOf { it.quantity }
                                    Text(
                                        if (totalQty > 0) "$totalQty" else "لا يوجد مخزون",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (totalQty == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onBarcodeScanned(manualInput) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("تعديل المنتج")
                                }
                                OutlinedButton(
                                    onClick = { onBarcodeScanned(manualInput) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Inventory, null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("عرض التفاصيل")
                                }
                            }
                        }
                    }
                }

                uiState.notFound && manualInput.length >= 3 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "لم يتم العثور على منتج",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "لا يوجد منتج بالرمز \"$manualInput\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { onBarcodeScanned(manualInput) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("إضافة منتج جديد بهذا الرمز")
                            }
                        }
                    }
                }

                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                else -> {
                    // Tips card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.TipsAndUpdates, null, tint = MaterialTheme.colorScheme.primary)
                                Text("💡 طرق البحث:", style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                "• اكتب رمز SKU يدوياً (مثال: ELEC-001)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• امسح باركود المنتج بكاميرا الجهاز\n  (يتطلب بناء APK على جهاز متصل بالإنترنت)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• وصّل جهاز باركود خارجي (USB أو Bluetooth)\n  وامسح الباركود مباشرة — سيُقرأ تلقائياً ✓",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• جهاز الباركود يجب أن يكون في وضع 'Keyboard Wedge'\n  أو 'HID Mode'",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

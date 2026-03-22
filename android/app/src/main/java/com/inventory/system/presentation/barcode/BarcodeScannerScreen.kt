package com.inventory.system.presentation.barcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var manualInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.foundProduct) {
        if (uiState.foundProduct != null) {
            // Product found - user can navigate to details
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بحث بالباركود / SKU") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, "مسح")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanner icon / header
            Icon(
                Icons.Default.QrCodeScanner,
                null,
                modifier = Modifier.size(80.dp).padding(vertical = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "ابحث عن المنتج بالباركود أو رمز SKU",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Search input
            OutlinedTextField(
                value = manualInput,
                onValueChange = {
                    manualInput = it
                    if (it.length >= 3) {
                        viewModel.searchBySku(it)
                    }
                },
                label = { Text("رمز المنتج أو الباركود (SKU)") },
                placeholder = { Text("مثال: ELEC-001") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
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
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                enabled = manualInput.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("بحث")
                }
            }

            Spacer(Modifier.height(24.dp))

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
                    // Product found card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        uiState.foundProduct!!.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "SKU: ${uiState.foundProduct!!.sku}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("الفئة", style = MaterialTheme.typography.labelSmall)
                                    Text(uiState.foundProduct!!.categoryName ?: "-")
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الحد الأدنى", style = MaterialTheme.typography.labelSmall)
                                    Text("${uiState.foundProduct!!.minStockLevel}")
                                }
                            }

                            Spacer(Modifier.height(8.dp))

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
                                    Text("تعديل")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onBarcodeScanned(manualInput)
                                    },
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
                uiState.notFound && manualInput.isNotBlank() -> {
                    // Product not found card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
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
                                "هل تريد إضافة منتج جديد بالرمز \"$manualInput\"؟",
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
                                Text("إضافة منتج جديد")
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {
                    // Empty state hints
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "💡 نصائح:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "• أدخل رمز SKU أو الباركود للمنتج",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• المسح بالكاميرا متاح عند اتصال الإنترنت",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "• يمكنك ربط أجهزة باركود خارجية",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

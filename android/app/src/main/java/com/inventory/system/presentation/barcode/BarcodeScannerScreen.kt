package com.inventory.system.presentation.barcode

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Note: Full barcode scanning requires CameraX + ML Kit dependencies.
// Add to build.gradle.kts when network is available:
//   implementation("com.google.mlkit:barcode-scanning:17.2.0")
//   implementation("androidx.camera:camera-camera2:1.3.4")
//   implementation("androidx.camera:camera-lifecycle:1.3.4")
//   implementation("androidx.camera:camera-view:1.3.4")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مسح الباركود") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Text("مسح الباركود", style = MaterialTheme.typography.headlineSmall)
            Text(
                "يحتاج هذا الخيار إلى تثبيت مكتبات CameraX و ML Kit. " +
                "أدخل رمز المنتج يدوياً في الوقت الحالي.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = manualInput,
                onValueChange = { manualInput = it },
                label = { Text("رمز المنتج (SKU)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (manualInput.isNotBlank()) onBarcodeScanned(manualInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = manualInput.isNotBlank()
            ) {
                Text("بحث")
            }
        }
    }
}

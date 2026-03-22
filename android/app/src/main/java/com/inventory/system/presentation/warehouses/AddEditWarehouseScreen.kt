package com.inventory.system.presentation.warehouses

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWarehouseScreen(
    warehouseId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: WarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = warehouseId != null

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    LaunchedEffect(warehouseId) {
        if (warehouseId != null) viewModel.loadWarehouse(warehouseId)
    }

    LaunchedEffect(uiState.selectedWarehouse) {
        uiState.selectedWarehouse?.let { w ->
            if (isEdit) {
                name = w.name
                location = w.location ?: ""
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
                title = { Text(if (isEdit) "تعديل مستودع" else "إضافة مستودع") },
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
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("اسم المستودع") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("الموقع (اختياري)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            uiState.actionError?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(err, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Button(
                onClick = {
                    val loc = location.ifBlank { null }
                    if (isEdit && warehouseId != null) {
                        viewModel.updateWarehouse(warehouseId, name, loc)
                    } else {
                        viewModel.createWarehouse(name, loc)
                    }
                },
                enabled = !uiState.isLoading && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (isEdit) "حفظ التعديلات" else "إضافة المستودع")
            }
        }
    }
}

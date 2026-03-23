package com.inventory.system.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.system.domain.model.DashboardStats
import com.inventory.system.presentation.auth.AuthViewModel
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBarcodeScanner: () -> Unit = {},
    onNavigateToCashier: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم") },
                actions = {
                    IconButton(onClick = onNavigateToBarcodeScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح باركود")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "تسجيل الخروج")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(
                message = uiState.error!!,
                onRetry = viewModel::loadStats,
                modifier = Modifier.padding(padding)
            )
            uiState.stats != null -> DashboardContent(
                stats = uiState.stats!!,
                onNavigateToCashier = onNavigateToCashier,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun DashboardContent(stats: DashboardStats, onNavigateToCashier: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("نظرة عامة", style = MaterialTheme.typography.titleLarge)

        // Cashier button — prominent, full-width
        Button(
            onClick = onNavigateToCashier,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("الكاشير 🛒", style = MaterialTheme.typography.titleMedium)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي المنتجات",
                value = stats.totalProducts.toString(),
                icon = Icons.Default.Inventory,
                color = MaterialTheme.colorScheme.primaryContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "المستودعات",
                value = stats.totalWarehouses.toString(),
                icon = Icons.Default.Warehouse,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "تنبيهات المخزون",
                value = stats.lowStockCount.toString(),
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.errorContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "الحركات الأخيرة",
                value = stats.recentMovements.toString(),
                icon = Icons.Default.SwapHoriz,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

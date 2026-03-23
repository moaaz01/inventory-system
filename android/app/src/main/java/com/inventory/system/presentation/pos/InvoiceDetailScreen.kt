package com.inventory.system.presentation.pos

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Int,
    onNavigateBack: () -> Unit,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(invoiceId) {
        viewModel.load(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الفاتورة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    uiState.invoice?.let { invoice ->
                        IconButton(onClick = {
                            val shareText = buildString {
                                appendLine("فاتورة: ${invoice.invoiceNumber}")
                                invoice.customerName?.let { appendLine("العميل: $it") }
                                appendLine("التاريخ: ${invoice.createdAt.take(10)}")
                                appendLine()
                                invoice.items.forEach { item ->
                                    appendLine("${item.productName} × ${item.quantity.toInt()} = ${"%.2f".format(item.totalPrice)}")
                                }
                                appendLine()
                                appendLine("المجموع الفرعي: ${"%.2f".format(invoice.subtotal)}")
                                if (invoice.discount > 0) appendLine("الخصم: -${"%.2f".format(invoice.discount)}")
                                appendLine("الإجمالي: ${"%.2f".format(invoice.total)}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "فاتورة ${invoice.invoiceNumber}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))
                        }) {
                            Icon(Icons.Default.Share, "مشاركة")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.invoice != null -> {
                val invoice = uiState.invoice!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Header card
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("فاتورة رقم: ${invoice.invoiceNumber}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                invoice.customerName?.let {
                                    Text("العميل: $it", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("التاريخ: ${invoice.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AssistChip(
                                    onClick = {},
                                    label = { Text(invoice.status) },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }

                    item {
                        Text("الأصناف (${invoice.items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    items(invoice.items) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("SKU: ${item.productSku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%.2f × %.0f".format(item.unitPrice, item.quantity), style = MaterialTheme.typography.bodySmall)
                                }
                                Text("%.2f".format(item.totalPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        // Summary
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المجموع الفرعي")
                                    Text("%.2f".format(invoice.subtotal))
                                }
                                if (invoice.discount > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("الخصم")
                                        Text("-%.2f".format(invoice.discount))
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الإجمالي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("%.2f".format(invoice.total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

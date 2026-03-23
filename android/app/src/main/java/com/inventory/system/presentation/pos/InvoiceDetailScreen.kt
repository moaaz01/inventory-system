package com.inventory.system.presentation.pos

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Int,
    onNavigateBack: () -> Unit,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        viewModel.load(invoiceId)
    }

    // Export dialog
    if (showExportDialog && uiState.invoice != null) {
        val invoice = uiState.invoice!!
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("تصدير الفاتورة") },
            text = { Text("اختر طريقة التصدير") },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    // Create PDF
                    try {
                        val document = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = document.startPage(pageInfo)
                        val canvas = page.canvas
                        val paint = Paint().apply {
                            color = Color.BLACK
                            textSize = 12f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            isAntiAlias = true
                        }

                        var y = 60f
                        val margin = 40f

                        // Header background
                        val headerPaint = Paint().apply { color = android.graphics.Color.parseColor("#1B5E20") }
                        canvas.drawRect(0f, 0f, 595f, 80f, headerPaint)

                        // Header text
                        paint.color = Color.WHITE
                        paint.textSize = 22f
                        canvas.drawText("فاتورة مبيعات", margin, 50f, paint)

                        y = 100f
                        paint.color = Color.BLACK
                        paint.textSize = 12f
                        canvas.drawText("رقم الفاتورة: ${invoice.invoiceNumber}", margin, y, paint); y += 20f
                        canvas.drawText("التاريخ: ${invoice.createdAt.take(10)}", margin, y, paint); y += 20f
                        canvas.drawText("العميل: ${invoice.customerName ?: "عميل"}", margin, y, paint); y += 30f

                        // Divider
                        val grayPaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f }
                        canvas.drawLine(margin, y, 555f, y, grayPaint)
                        y += 10f

                        // Table header
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("المنتج", margin, y, paint)
                        canvas.drawText("الكمية", 300f, y, paint)
                        canvas.drawText("السعر", 380f, y, paint)
                        canvas.drawText("الإجمالي", 470f, y, paint)
                        y += 5f
                        canvas.drawLine(margin, y, 555f, y, grayPaint)
                        y += 15f

                        // Items
                        paint.typeface = Typeface.DEFAULT
                        invoice.items.forEach { item ->
                            canvas.drawText(item.productName.take(25), margin, y, paint)
                            canvas.drawText(item.quantity.toInt().toString(), 300f, y, paint)
                            canvas.drawText("%.2f".format(item.unitPrice), 380f, y, paint)
                            canvas.drawText("%.2f".format(item.totalPrice), 470f, y, paint)
                            y += 22f
                        }

                        y += 5f
                        canvas.drawLine(margin, y, 555f, y, grayPaint)
                        y += 20f

                        paint.textSize = 13f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("المجموع الفرعي: ${"%.2f".format(invoice.subtotal)} ر.س", 320f, y, paint); y += 25f
                        if (invoice.discount > 0) {
                            canvas.drawText("الخصم: -${"%.2f".format(invoice.discount)} ر.س", 320f, y, paint); y += 25f
                        }
                        paint.textSize = 16f
                        canvas.drawText("الإجمالي: ${"%.2f".format(invoice.total)} ر.س", 320f, y, paint)

                        document.finishPage(page)

                        val file = File(context.cacheDir, "invoice_${invoice.invoiceNumber}.pdf")
                        document.writeTo(FileOutputStream(file))
                        document.close()

                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "فتح الفاتورة"))
                    } catch (_: Exception) {}
                }) { Text("تصدير PDF") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    // Share as text
                    val invoice = uiState.invoice!!
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
                }) { Text("مشاركة نصي") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الفاتورة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    uiState.invoice?.let {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Share, "تصدير")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ComposeColor(0xFF1B5E20),
                    titleContentColor = ComposeColor.White,
                    navigationIconContentColor = ComposeColor.White,
                    actionIconContentColor = ComposeColor.White
                )
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor(0xFFF5F5F5))
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Receipt-style header card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF1B5E20))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "فاتورة رقم: ${invoice.invoiceNumber}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ComposeColor.White
                                )
                                invoice.customerName?.let {
                                    Text("العميل: $it", style = MaterialTheme.typography.bodyMedium, color = ComposeColor(0xFFB9F6CA))
                                }
                                Text(
                                    "التاريخ: ${invoice.createdAt.take(10)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComposeColor(0xFF81C784)
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = ComposeColor(0xFF4CAF50)
                                ) {
                                    Text(
                                        invoice.status,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ComposeColor.White
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "الأصناف (${invoice.items.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    items(invoice.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("SKU: ${item.productSku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%.2f × %.0f".format(item.unitPrice, item.quantity), style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    "%.2f".format(item.totalPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ComposeColor(0xFF1B5E20)
                                )
                            }
                        }
                    }

                    item {
                        // Summary footer card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(3.dp),
                            colors = CardDefaults.cardColors(containerColor = ComposeColor.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المجموع الفرعي", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%.2f".format(invoice.subtotal))
                                }
                                if (invoice.discount > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("الخصم", color = MaterialTheme.colorScheme.error)
                                        Text("-%.2f".format(invoice.discount), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                HorizontalDivider()
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "الإجمالي",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ComposeColor(0xFF1B5E20)
                                    )
                                    Text(
                                        "%.2f ر.س".format(invoice.total),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ComposeColor(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

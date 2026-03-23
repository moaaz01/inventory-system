package com.inventory.system.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")

    // Main bottom nav
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object Warehouses : Screen("warehouses")
    object StockOperations : Screen("stock_operations")
    object Reports : Screen("reports")

    // Detail screens
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: Int? = null) = if (productId != null) "add_edit_product?productId=$productId" else "add_edit_product"
    }
    object WarehouseDetail : Screen("warehouse_detail/{warehouseId}") {
        fun createRoute(warehouseId: Int) = "warehouse_detail/$warehouseId"
    }
    object AddEditWarehouse : Screen("add_edit_warehouse?warehouseId={warehouseId}") {
        fun createRoute(warehouseId: Int? = null) = if (warehouseId != null) "add_edit_warehouse?warehouseId=$warehouseId" else "add_edit_warehouse"
    }
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object BarcodeScanner : Screen("barcode_scanner")
    object Users : Screen("users")
    object ExportImport : Screen("export_import")
    object AddEditProductWithSku : Screen("add_edit_product?productId={productId}&sku={sku}") {
        fun createRouteWithSku(sku: String) = "add_edit_product?sku=$sku"
    }

    object Cashier : Screen("cashier")
    object InvoiceHistory : Screen("invoice_history")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Int) = "invoice_detail/$invoiceId"
    }
}

enum class BottomNavItem(
    val screen: Screen,
    val labelRes: String,
    val icon: ImageVector,
    val contentDesc: String
) {
    DASHBOARD(Screen.Dashboard, "الرئيسية", Icons.Default.Dashboard, "لوحة التحكم"),
    PRODUCTS(Screen.Products, "المنتجات", Icons.Default.Inventory, "قائمة المنتجات"),
    CATEGORIES(Screen.Categories, "الفئات", Icons.Default.Category, "تصفح الفئات"),
    WAREHOUSES(Screen.Warehouses, "المستودعات", Icons.Default.Warehouse, "إدارة المستودعات"),
    STOCK(Screen.StockOperations, "العمليات", Icons.Default.SwapHoriz, "عمليات المخزون")
}

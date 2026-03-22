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
}

enum class BottomNavItem(
    val screen: Screen,
    val labelRes: String,
    val icon: ImageVector
) {
    DASHBOARD(Screen.Dashboard, "لوحة التحكم", Icons.Default.Dashboard),
    PRODUCTS(Screen.Products, "المنتجات", Icons.Default.Inventory),
    WAREHOUSES(Screen.Warehouses, "المستودعات", Icons.Default.Warehouse),
    STOCK(Screen.StockOperations, "العمليات", Icons.Default.SwapHoriz),
    REPORTS(Screen.Reports, "التقارير", Icons.Default.Assessment)
}

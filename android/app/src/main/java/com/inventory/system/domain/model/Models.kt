package com.inventory.system.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val isActive: Boolean = true
)

data class Product(
    val id: Int,
    val sku: String,
    val name: String,
    val categoryId: Int?,
    val categoryName: String?,
    val unitId: Int?,
    val unitName: String?,
    val minStockLevel: Int,
    val stockInfo: List<StockInfo> = emptyList()
)

data class Category(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val children: List<Category> = emptyList()
)

data class Unit(
    val id: Int,
    val name: String,
    val symbol: String
)

data class Warehouse(
    val id: Int,
    val name: String,
    val location: String?,
    val stockInfo: List<StockInfo> = emptyList()
)

data class StockInfo(
    val productId: Int?,
    val warehouseId: Int?,
    val quantity: Int,
    val productName: String? = null,
    val warehouseName: String? = null
)

data class Movement(
    val id: Int,
    val type: String,
    val productId: Int,
    val warehouseId: Int?,
    val fromWarehouseId: Int?,
    val toWarehouseId: Int?,
    val quantity: Int,
    val referenceNumber: String?,
    val notes: String?,
    val createdAt: String,
    val productName: String? = null,
    val warehouseName: String? = null
)

data class DashboardStats(
    val totalProducts: Int,
    val totalWarehouses: Int,
    val totalStockValue: Double,
    val lowStockCount: Int,
    val recentMovements: Int
)

data class InventoryReportItem(
    val productId: Int,
    val productName: String,
    val sku: String,
    val totalQuantity: Int,
    val minStockLevel: Int,
    val isLowStock: Boolean
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

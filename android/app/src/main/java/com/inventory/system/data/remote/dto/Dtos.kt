package com.inventory.system.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @com.google.gson.annotations.SerializedName("is_active") val isActive: Boolean = true
)

data class ProductDto(
    val id: Int,
    val sku: String,
    val name: String,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("unit_id") val unitId: Int?,
    @SerializedName("min_stock_level") val minStockLevel: Int,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("unit_name") val unitName: String?,
    // Products API returns nested stock as "stock" array with {warehouse_id, warehouse_name, quantity}
    val stock: List<ProductStockDto>? = null,
    @SerializedName("retail_price") val retailPrice: Double? = null,
    @SerializedName("wholesale_price") val wholesalePrice: Double? = null,
    val currency: String = "USD"
)

data class ProductStockDto(
    @SerializedName("warehouse_id") val warehouseId: Int,
    @SerializedName("warehouse_name") val warehouseName: String,
    val quantity: Int
)

data class ProductCreateRequest(
    val sku: String,
    val name: String,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("unit_id") val unitId: Int?,
    @SerializedName("min_stock_level") val minStockLevel: Int,
    @SerializedName("retail_price") val retailPrice: Double? = null,
    @SerializedName("wholesale_price") val wholesalePrice: Double? = null,
    val currency: String = "USD"
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val size: Int
)

data class CategoryDto(
    val id: Int,
    val name: String,
    @SerializedName("parent_id") val parentId: Int?,
    val children: List<CategoryDto>?
)

data class UnitDto(
    val id: Int,
    val name: String,
    val symbol: String
)

data class WarehouseDto(
    val id: Int,
    val name: String,
    val location: String?,
    @SerializedName("stock_items") val stockItems: List<WarehouseStockItemDto>? = null
)

data class WarehouseStockItemDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    val quantity: Int
)

// /api/stock returns flat items with product_name, warehouse_name, is_low_stock etc.
data class StockInfoDto(
    val id: Int? = null,
    @SerializedName("product_id") val productId: Int?,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("warehouse_id") val warehouseId: Int?,
    @SerializedName("warehouse_name") val warehouseName: String? = null,
    val quantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int? = null,
    @SerializedName("is_low_stock") val isLowStock: Boolean? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

// Movements API returns flat fields
data class MovementDto(
    val id: Int,
    @SerializedName("movement_type") val movementType: String,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("warehouse_id") val warehouseId: Int?,
    @SerializedName("warehouse_name") val warehouseName: String? = null,
    @SerializedName("to_warehouse_id") val toWarehouseId: Int?,
    val quantity: Int,
    @SerializedName("reference_number") val referenceNumber: String?,
    val notes: String?,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("created_at") val createdAt: String
)

data class ReceiptRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("warehouse_id") val warehouseId: Int,
    val quantity: Int,
    @SerializedName("reference_number") val referenceNumber: String?,
    val notes: String?
)

data class IssueRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("warehouse_id") val warehouseId: Int,
    val quantity: Int,
    @SerializedName("reference_number") val referenceNumber: String?
)

data class TransferRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("from_warehouse_id") val fromWarehouseId: Int,
    @SerializedName("to_warehouse_id") val toWarehouseId: Int,
    val quantity: Int
)

data class DashboardStatsDto(
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("total_warehouses") val totalWarehouses: Int,
    @SerializedName("total_stock_value") val totalStockValue: Double,
    @SerializedName("low_stock_count") val lowStockCount: Int,
    @SerializedName("recent_movements") val recentMovements: Int
)

// /api/reports/inventory returns per-warehouse rows with quantity per row
data class InventoryReportItemDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    val sku: String,
    @SerializedName("warehouse_id") val warehouseId: Int? = null,
    @SerializedName("warehouse_name") val warehouseName: String? = null,
    val quantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int,
    @SerializedName("is_low_stock") val isLowStock: Boolean
)

// /api/reports/low-stock returns flat items (not Product shape)
data class LowStockItemDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    val sku: String,
    @SerializedName("warehouse_id") val warehouseId: Int,
    @SerializedName("warehouse_name") val warehouseName: String,
    val quantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int
)

data class ImportResult(
    val success: Boolean,
    val message: String,
    val count: Int = 0
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

data class UpdateUserRequest(
    val role: String? = null,
    @com.google.gson.annotations.SerializedName("is_active") val isActive: Boolean? = null
)

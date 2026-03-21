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
    val role: String
)

data class ProductDto(
    val id: Int,
    val sku: String,
    val name: String,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("unit_id") val unitId: Int?,
    @SerializedName("min_stock_level") val minStockLevel: Int,
    val category: CategoryDto?,
    val unit: UnitDto?,
    @SerializedName("stock_info") val stockInfo: List<StockInfoDto>?
)

data class ProductCreateRequest(
    val sku: String,
    val name: String,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("unit_id") val unitId: Int?,
    @SerializedName("min_stock_level") val minStockLevel: Int
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
    @SerializedName("stock_info") val stockInfo: List<StockInfoDto>?
)

data class StockInfoDto(
    @SerializedName("product_id") val productId: Int?,
    @SerializedName("warehouse_id") val warehouseId: Int?,
    val quantity: Int,
    val product: ProductDto?,
    val warehouse: WarehouseDto?
)

data class MovementDto(
    val id: Int,
    val type: String,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("warehouse_id") val warehouseId: Int?,
    @SerializedName("from_warehouse_id") val fromWarehouseId: Int?,
    @SerializedName("to_warehouse_id") val toWarehouseId: Int?,
    val quantity: Int,
    @SerializedName("reference_number") val referenceNumber: String?,
    val notes: String?,
    @SerializedName("created_at") val createdAt: String,
    val product: ProductDto?,
    val warehouse: WarehouseDto?
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

data class InventoryReportItemDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    val sku: String,
    @SerializedName("total_quantity") val totalQuantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int,
    @SerializedName("is_low_stock") val isLowStock: Boolean
)

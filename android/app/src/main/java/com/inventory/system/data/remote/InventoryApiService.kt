package com.inventory.system.data.remote

import com.inventory.system.data.remote.dto.*
import retrofit2.http.*

interface InventoryApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @GET("api/auth/me")
    suspend fun getMe(): UserDto

    // Products
    @GET("api/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): PaginatedResponse<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @POST("api/products")
    suspend fun createProduct(@Body request: ProductCreateRequest): ProductDto

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body request: ProductCreateRequest): ProductDto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)

    // Categories
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("api/categories")
    suspend fun createCategory(@Body body: Map<String, String>): CategoryDto

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body body: Map<String, String>): CategoryDto

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int)

    // Units
    @GET("api/units")
    suspend fun getUnits(): List<UnitDto>

    @POST("api/units")
    suspend fun createUnit(@Body body: Map<String, String>): UnitDto

    // Warehouses
    @GET("api/warehouses")
    suspend fun getWarehouses(): List<WarehouseDto>

    @GET("api/warehouses/{id}")
    suspend fun getWarehouse(@Path("id") id: Int): WarehouseDto

    @POST("api/warehouses")
    suspend fun createWarehouse(@Body body: Map<String, String>): WarehouseDto

    @PUT("api/warehouses/{id}")
    suspend fun updateWarehouse(@Path("id") id: Int, @Body body: Map<String, String>): WarehouseDto

    @DELETE("api/warehouses/{id}")
    suspend fun deleteWarehouse(@Path("id") id: Int)

    // Stock
    @GET("api/stock")
    suspend fun getStock(
        @Query("warehouse_id") warehouseId: Int? = null,
        @Query("product_id") productId: Int? = null,
        @Query("low_stock") lowStock: Boolean? = null
    ): List<StockInfoDto>

    // Movements
    @GET("api/movements")
    suspend fun getMovements(
        @Query("type") type: String? = null,
        @Query("product_id") productId: Int? = null,
        @Query("warehouse_id") warehouseId: Int? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): PaginatedResponse<MovementDto>

    @POST("api/movements/receipt")
    suspend fun receipt(@Body request: ReceiptRequest): MovementDto

    @POST("api/movements/issue")
    suspend fun issue(@Body request: IssueRequest): MovementDto

    @POST("api/movements/transfer")
    suspend fun transfer(@Body request: TransferRequest): MovementDto

    // Reports
    @GET("api/reports/inventory")
    suspend fun getInventoryReport(): List<InventoryReportItemDto>

    @GET("api/reports/low-stock")
    suspend fun getLowStockReport(): List<ProductDto>

    @GET("api/reports/movements")
    suspend fun getMovementsReport(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("movement_type") movementType: String? = null
    ): List<MovementDto>

    // Dashboard
    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsDto
}

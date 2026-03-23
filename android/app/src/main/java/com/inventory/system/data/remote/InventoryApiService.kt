package com.inventory.system.data.remote

import com.inventory.system.data.remote.dto.*
import com.inventory.system.data.remote.dto.NextSkuDto
import retrofit2.Response
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
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    // Categories
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("api/categories")
    suspend fun createCategory(@Body body: Map<String, String>): CategoryDto

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body body: Map<String, String>): CategoryDto

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

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
    suspend fun deleteWarehouse(@Path("id") id: Int): Response<Unit>

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
        @Query("movement_type") type: String? = null,
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
    suspend fun getLowStockReport(): List<LowStockItemDto>

    @GET("api/reports/movements")
    suspend fun getMovementsReport(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("movement_type") movementType: String? = null
    ): List<MovementDto>

    // Dashboard
    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsDto

    @GET("api/products/next-sku/{categoryId}")
    suspend fun getNextSku(@Path("categoryId") categoryId: Int): NextSkuDto

    // All products (no paging) for dropdowns - uses /api/products/all
    @GET("api/products/all")
    suspend fun getAllProducts(): List<ProductDto>

    // Export
    @GET("api/export/products")
    suspend fun exportProducts(): okhttp3.ResponseBody

    @GET("api/export/warehouses")
    suspend fun exportWarehouses(): okhttp3.ResponseBody

    @GET("api/export/all")
    suspend fun exportAll(): okhttp3.ResponseBody

    // Import
    @Multipart
    @POST("api/import/products")
    suspend fun importProducts(@Part file: okhttp3.MultipartBody.Part): ImportResult

    @Multipart
    @POST("api/import/warehouses")
    suspend fun importWarehouses(@Part file: okhttp3.MultipartBody.Part): ImportResult

    // Users (admin)
    @GET("api/users")
    suspend fun getUsers(): List<UserDto>

    @POST("api/users")
    suspend fun createUser(@Body data: CreateUserRequest): UserDto

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body data: UpdateUserRequest): UserDto

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // Invoices
    @POST("api/invoices")
    suspend fun createInvoice(@Body data: CreateInvoiceDto): InvoiceDto

    @GET("api/invoices")
    suspend fun getInvoices(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("search") search: String? = null
    ): List<InvoiceDto>

    @GET("api/invoices/{id}")
    suspend fun getInvoice(@Path("id") id: Int): InvoiceDto

    @GET("api/invoices/by-number/{invoiceNumber}")
    suspend fun getInvoiceByNumber(@Path("invoiceNumber") invoiceNumber: String): InvoiceDto
}

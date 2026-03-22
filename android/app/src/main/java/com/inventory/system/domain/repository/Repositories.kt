package com.inventory.system.domain.repository

import androidx.paging.PagingData
import com.inventory.system.domain.model.*
import com.inventory.system.domain.model.Unit as InventoryUnit
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(username: String, email: String, password: String): Result<User>
    suspend fun getMe(): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
}

interface ProductRepository {
    fun getProducts(search: String? = null, categoryId: Int? = null): Flow<PagingData<Product>>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun createProduct(sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int): Result<Product>
    suspend fun updateProduct(id: Int, sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int): Result<Product>
    suspend fun deleteProduct(id: Int): Result<Unit>
    fun getCachedProducts(): Flow<List<Product>>
}

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    fun getCachedCategories(): Flow<List<Category>>
    suspend fun createCategory(name: String): Result<Category>
    suspend fun updateCategory(id: Int, name: String): Result<Category>
    suspend fun deleteCategory(id: Int): Result<Unit>
}

interface WarehouseRepository {
    suspend fun getWarehouses(): Result<List<Warehouse>>
    fun getCachedWarehouses(): Flow<List<Warehouse>>
    suspend fun getWarehouse(id: Int): Result<Warehouse>
    suspend fun createWarehouse(name: String, location: String?): Result<Warehouse>
    suspend fun updateWarehouse(id: Int, name: String, location: String?): Result<Warehouse>
}

interface StockRepository {
    suspend fun getStock(warehouseId: Int? = null, productId: Int? = null, lowStock: Boolean? = null): Result<List<StockInfo>>
}

interface MovementRepository {
    fun getMovements(type: String? = null, productId: Int? = null, warehouseId: Int? = null): Flow<PagingData<Movement>>
    suspend fun receipt(productId: Int, warehouseId: Int, quantity: Int, referenceNumber: String?, notes: String?): Result<Movement>
    suspend fun issue(productId: Int, warehouseId: Int, quantity: Int, referenceNumber: String?): Result<Movement>
    suspend fun transfer(productId: Int, fromWarehouseId: Int, toWarehouseId: Int, quantity: Int): Result<Movement>
}

interface ReportRepository {
    suspend fun getInventoryReport(): Result<List<InventoryReportItem>>
    suspend fun getLowStockReport(): Result<List<Product>>
    suspend fun getMovementsReport(fromDate: String? = null, toDate: String? = null, movementType: String? = null): Result<List<Movement>>
    suspend fun getDashboardStats(): Result<DashboardStats>
}

interface UnitRepository {
    suspend fun getUnits(): Result<List<InventoryUnit>>
    fun getCachedUnits(): Flow<List<InventoryUnit>>
}

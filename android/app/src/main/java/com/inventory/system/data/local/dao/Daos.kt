package com.inventory.system.data.local.dao

import androidx.room.*
import com.inventory.system.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :search || '%' OR sku LIKE '%' || :search || '%'")
    fun searchProducts(search: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses ORDER BY name")
    fun getAllWarehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouse(id: Int): WarehouseEntity?

    @Upsert
    suspend fun upsertAll(warehouses: List<WarehouseEntity>)

    @Upsert
    suspend fun upsert(warehouse: WarehouseEntity)

    @Query("DELETE FROM warehouses")
    suspend fun deleteAll()
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY name")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Upsert
    suspend fun upsertAll(units: List<UnitEntity>)

    @Query("DELETE FROM units")
    suspend fun deleteAll()
}

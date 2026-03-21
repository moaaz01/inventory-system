package com.inventory.system.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val sku: String,
    val name: String,
    val categoryId: Int?,
    val unitId: Int?,
    val minStockLevel: Int,
    val categoryName: String?,
    val unitName: String?
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val parentId: Int?
)

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val location: String?
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val symbol: String
)

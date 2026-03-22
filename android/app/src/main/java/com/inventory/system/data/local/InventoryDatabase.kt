package com.inventory.system.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inventory.system.data.local.dao.*
import com.inventory.system.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        WarehouseEntity::class,
        UnitEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun unitDao(): UnitDao
}

package com.inventory.system.data.repository

import com.inventory.system.data.local.entity.*
import com.inventory.system.data.remote.dto.*
import com.inventory.system.domain.model.*
import com.inventory.system.domain.model.Unit

// DTO -> Domain
fun ProductDto.toDomain() = Product(
    id = id,
    sku = sku,
    name = name,
    categoryId = categoryId,
    categoryName = category?.name,
    unitId = unitId,
    unitName = unit?.name,
    minStockLevel = minStockLevel,
    stockInfo = stockInfo?.map { it.toDomain() } ?: emptyList()
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    parentId = parentId,
    children = children?.map { it.toDomain() } ?: emptyList()
)

fun UnitDto.toDomain() = Unit(id = id, name = name, symbol = symbol)

fun WarehouseDto.toDomain() = Warehouse(
    id = id,
    name = name,
    location = location,
    stockInfo = stockInfo?.map { it.toDomain() } ?: emptyList()
)

fun StockInfoDto.toDomain() = StockInfo(
    productId = productId,
    warehouseId = warehouseId,
    quantity = quantity,
    productName = product?.name,
    warehouseName = warehouse?.name
)

fun MovementDto.toDomain() = Movement(
    id = id,
    type = type,
    productId = productId,
    warehouseId = warehouseId,
    fromWarehouseId = fromWarehouseId,
    toWarehouseId = toWarehouseId,
    quantity = quantity,
    referenceNumber = referenceNumber,
    notes = notes,
    createdAt = createdAt,
    productName = product?.name,
    warehouseName = warehouse?.name
)

fun DashboardStatsDto.toDomain() = DashboardStats(
    totalProducts = totalProducts,
    totalWarehouses = totalWarehouses,
    totalStockValue = totalStockValue,
    lowStockCount = lowStockCount,
    recentMovements = recentMovements
)

fun InventoryReportItemDto.toDomain() = InventoryReportItem(
    productId = productId,
    productName = productName,
    sku = sku,
    totalQuantity = totalQuantity,
    minStockLevel = minStockLevel,
    isLowStock = isLowStock
)

fun UserDto.toDomain() = User(id = id, username = username, email = email, role = role)

// DTO -> Entity
fun ProductDto.toEntity() = ProductEntity(
    id = id, sku = sku, name = name, categoryId = categoryId,
    unitId = unitId, minStockLevel = minStockLevel,
    categoryName = category?.name, unitName = unit?.name
)

fun CategoryDto.toEntity() = CategoryEntity(id = id, name = name, parentId = parentId)

fun WarehouseDto.toEntity() = WarehouseEntity(id = id, name = name, location = location)

fun UnitDto.toEntity() = UnitEntity(id = id, name = name, symbol = symbol)

// Entity -> Domain
fun ProductEntity.toDomain() = Product(
    id = id, sku = sku, name = name, categoryId = categoryId,
    categoryName = categoryName, unitId = unitId, unitName = unitName,
    minStockLevel = minStockLevel
)

fun CategoryEntity.toDomain() = Category(id = id, name = name, parentId = parentId)

fun WarehouseEntity.toDomain() = Warehouse(id = id, name = name, location = location)

fun UnitEntity.toDomain() = Unit(id = id, name = name, symbol = symbol)

package com.inventory.system.data.repository

import com.inventory.system.data.local.entity.*
import com.inventory.system.data.remote.dto.*
import com.inventory.system.domain.model.NextSku
import com.inventory.system.domain.model.*
import com.inventory.system.domain.model.Unit as InventoryUnit

fun NextSkuDto.toDomain() = NextSku(sku = sku, prefix = prefix)

// DTO -> Domain
fun ProductDto.toDomain() = Product(
    id = id,
    sku = sku,
    name = name,
    categoryId = categoryId,
    categoryName = categoryName,
    unitId = unitId,
    unitName = unitName,
    minStockLevel = minStockLevel,
    stockInfo = stock?.map { StockInfo(productId = id, warehouseId = it.warehouseId, quantity = it.quantity, warehouseName = it.warehouseName) } ?: emptyList(),
    retailPrice = retailPrice,
    wholesalePrice = wholesalePrice,
    currency = currency
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    parentId = parentId,
    children = children?.map { it.toDomain() } ?: emptyList()
)

fun UnitDto.toDomain() = InventoryUnit(id = id, name = name, symbol = symbol)

fun WarehouseDto.toDomain() = Warehouse(
    id = id,
    name = name,
    location = location,
    stockInfo = stockItems?.map { StockInfo(productId = it.productId, warehouseId = id, quantity = it.quantity, productName = it.productName) } ?: emptyList()
)

fun StockInfoDto.toDomain() = StockInfo(
    productId = productId,
    warehouseId = warehouseId,
    quantity = quantity,
    productName = productName,
    warehouseName = warehouseName
)

fun MovementDto.toDomain() = Movement(
    id = id,
    type = movementType,
    productId = productId,
    warehouseId = warehouseId,
    fromWarehouseId = null,
    toWarehouseId = toWarehouseId,
    quantity = quantity,
    referenceNumber = referenceNumber,
    notes = notes,
    createdAt = createdAt,
    productName = productName,
    warehouseName = warehouseName
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
    totalQuantity = quantity,
    minStockLevel = minStockLevel,
    isLowStock = isLowStock
)

fun LowStockItemDto.toDomain() = Product(
    id = productId,
    sku = sku,
    name = productName,
    categoryId = null,
    categoryName = null,
    unitId = null,
    unitName = null,
    minStockLevel = minStockLevel,
    stockInfo = listOf(StockInfo(productId = productId, warehouseId = warehouseId, quantity = quantity, warehouseName = warehouseName)),
    retailPrice = null,
    wholesalePrice = null,
    currency = "USD"
)

fun UserDto.toDomain() = User(id = id, username = username, email = email, role = role, isActive = isActive)

// DTO -> Entity
fun ProductDto.toEntity() = ProductEntity(
    id = id, sku = sku, name = name, categoryId = categoryId,
    unitId = unitId, minStockLevel = minStockLevel,
    categoryName = categoryName, unitName = unitName,
    retailPrice = retailPrice?.toFloat(),
    wholesalePrice = wholesalePrice?.toFloat(),
    currency = currency
)

fun CategoryDto.toEntity() = CategoryEntity(id = id, name = name, parentId = parentId)

fun WarehouseDto.toEntity() = WarehouseEntity(id = id, name = name, location = location)

fun UnitDto.toEntity() = UnitEntity(id = id, name = name, symbol = symbol)

// Entity -> Domain
fun ProductEntity.toDomain() = Product(
    id = id, sku = sku, name = name, categoryId = categoryId,
    categoryName = categoryName, unitId = unitId, unitName = unitName,
    minStockLevel = minStockLevel,
    retailPrice = retailPrice?.toDouble(),
    wholesalePrice = wholesalePrice?.toDouble(),
    currency = currency
)

fun CategoryEntity.toDomain() = Category(id = id, name = name, parentId = parentId)

fun WarehouseEntity.toDomain() = Warehouse(id = id, name = name, location = location)

fun UnitEntity.toDomain() = InventoryUnit(id = id, name = name, symbol = symbol)

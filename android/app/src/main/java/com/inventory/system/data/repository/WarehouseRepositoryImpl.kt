package com.inventory.system.data.repository

import com.inventory.system.data.local.dao.WarehouseDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Warehouse
import com.inventory.system.domain.repository.WarehouseRepository
import com.inventory.system.util.DebugLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "WarehouseRepository"

class WarehouseRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val warehouseDao: WarehouseDao
) : WarehouseRepository {

    override suspend fun getWarehouses(): Result<List<Warehouse>> {
        DebugLogger.logApiCall(TAG, "/api/warehouses")
        return safeApiCall {
            val dtos = api.getWarehouses()
            DebugLogger.logDbOperation(TAG, "Upsert", "warehouses", dtos.size)
            warehouseDao.upsertAll(dtos.map { it.toEntity() })
            val result = dtos.map { it.toDomain() }
            DebugLogger.logApiResponse(TAG, "/api/warehouses", true, result.size)
            DebugLogger.d(TAG, "Warehouses loaded: ${result.size}")
            result.forEach { wh ->
                DebugLogger.d(TAG, "  - ${wh.name}: ${wh.stockInfo.size} stock items")
            }
            DebugLogger.verifyExecution(TAG, "getWarehouses completed")
            result
        }
    }

    override fun getCachedWarehouses(): Flow<List<Warehouse>> =
        warehouseDao.getAllWarehouses().map { list -> 
            val result = list.map { it.toDomain() }
            DebugLogger.logDbOperation(TAG, "Read (cached)", "warehouses", result.size)
            result
        }

    override suspend fun getWarehouse(id: Int): Result<Warehouse> {
        DebugLogger.logApiCall(TAG, "/api/warehouses/$id")
        return safeApiCall {
            val result = api.getWarehouse(id).toDomain()
            DebugLogger.logApiResponse(TAG, "/api/warehouses/$id", true, 1)
            DebugLogger.d(TAG, "Warehouse detail: ${result.name}, stock: ${result.stockInfo.size}")
            DebugLogger.verifyExecution(TAG, "getWarehouse completed")
            result
        }
    }

    override suspend fun createWarehouse(name: String, location: String?): Result<Warehouse> {
        DebugLogger.logApiCall(TAG, "/api/warehouses (POST)")
        DebugLogger.logUserAction(TAG, "createWarehouse", mapOf("name" to name, "location" to (location ?: "null")))
        return safeApiCall {
            val body = mutableMapOf("name" to name)
            location?.let { body["location"] = it }
            val result = api.createWarehouse(body).toDomain()
            DebugLogger.logApiResponse(TAG, "/api/warehouses (POST)", true, 1)
            DebugLogger.verifyExecution(TAG, "createWarehouse completed")
            result
        }
    }

    override suspend fun updateWarehouse(id: Int, name: String, location: String?): Result<Warehouse> {
        DebugLogger.logApiCall(TAG, "/api/warehouses/$id (PUT)")
        DebugLogger.logUserAction(TAG, "updateWarehouse", mapOf("id" to id, "name" to name))
        return safeApiCall {
            val body = mutableMapOf("name" to name)
            location?.let { body["location"] = it }
            val dto = api.updateWarehouse(id, body)
            warehouseDao.upsertAll(listOf(dto.toEntity()))
            val result = dto.toDomain()
            DebugLogger.logApiResponse(TAG, "/api/warehouses/$id (PUT)", true, 1)
            DebugLogger.verifyExecution(TAG, "updateWarehouse completed")
            result
        }
    }
}

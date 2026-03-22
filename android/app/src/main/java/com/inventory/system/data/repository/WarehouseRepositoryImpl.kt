package com.inventory.system.data.repository

import com.inventory.system.data.local.dao.WarehouseDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Warehouse
import com.inventory.system.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WarehouseRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val warehouseDao: WarehouseDao
) : WarehouseRepository {

    override suspend fun getWarehouses(): Result<List<Warehouse>> = safeApiCall {
        val dtos = api.getWarehouses()
        warehouseDao.upsertAll(dtos.map { it.toEntity() })
        dtos.map { it.toDomain() }
    }

    override fun getCachedWarehouses(): Flow<List<Warehouse>> =
        warehouseDao.getAllWarehouses().map { list -> list.map { it.toDomain() } }

    override suspend fun getWarehouse(id: Int): Result<Warehouse> = safeApiCall {
        api.getWarehouse(id).toDomain()
    }

    override suspend fun createWarehouse(name: String, location: String?): Result<Warehouse> = safeApiCall {
        val body = mutableMapOf("name" to name)
        location?.let { body["location"] = it }
        api.createWarehouse(body).toDomain()
    }

    override suspend fun updateWarehouse(id: Int, name: String, location: String?): Result<Warehouse> = safeApiCall {
        val body = mutableMapOf("name" to name)
        location?.let { body["location"] = it }
        val dto = api.updateWarehouse(id, body)
        warehouseDao.upsertAll(listOf(dto.toEntity()))
        dto.toDomain()
    }
}

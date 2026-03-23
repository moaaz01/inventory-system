package com.inventory.system.data.repository

import com.inventory.system.data.local.dao.UnitDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Unit as InventoryUnit
import com.inventory.system.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UnitRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val unitDao: UnitDao
) : UnitRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getUnits(): Result<List<InventoryUnit>> = safeApiCall {
        val dtos = api.getUnits()
        unitDao.upsertAll(dtos.map { it.toEntity() })
        dtos.map { it.toDomain() }
    }

    override fun getCachedUnits(): Flow<List<InventoryUnit>> =
        unitDao.getAllUnits().map { list -> list.map { it.toDomain() } }
}

package com.inventory.system.data.repository

import com.inventory.system.data.local.dao.UnitDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Unit
import com.inventory.system.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UnitRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val unitDao: UnitDao
) : UnitRepository {

    override suspend fun getUnits(): Result<List<Unit>> = safeApiCall {
        val dtos = api.getUnits()
        unitDao.upsertAll(dtos.map { it.toEntity() })
        dtos.map { it.toDomain() }
    }

    override fun getCachedUnits(): Flow<List<Unit>> =
        unitDao.getAllUnits().map { list -> list.map { it.toDomain() } }
}

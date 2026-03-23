package com.inventory.system.data.repository

import com.inventory.system.data.local.dao.CategoryDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Category
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val categoryDao: CategoryDao
) : CategoryRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getCategories(): Result<List<Category>> = safeApiCall {
        val dtos = api.getCategories()
        categoryDao.upsertAll(dtos.map { it.toEntity() })
        dtos.map { it.toDomain() }
    }

    override fun getCachedCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun createCategory(name: String): Result<Category> = safeApiCall {
        api.createCategory(mapOf("name" to name)).toDomain()
    }

    override suspend fun updateCategory(id: Int, name: String): Result<Category> = safeApiCall {
        val dto = api.updateCategory(id, mapOf("name" to name))
        categoryDao.upsertAll(listOf(dto.toEntity()))
        dto.toDomain()
    }

    override suspend fun deleteCategory(id: Int): Result<kotlin.Unit> = safeApiCall {
        api.deleteCategory(id)
        categoryDao.deleteById(id)
    }
}

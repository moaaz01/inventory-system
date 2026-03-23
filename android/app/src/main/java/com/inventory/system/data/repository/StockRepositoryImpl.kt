package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.StockInfo
import com.inventory.system.domain.repository.StockRepository
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : StockRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getStock(warehouseId: Int?, productId: Int?, lowStock: Boolean?): Result<List<StockInfo>> = safeApiCall {
        api.getStock(warehouseId, productId, lowStock).map { it.toDomain() }
    }
}

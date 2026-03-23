package com.inventory.system.data.repository

import androidx.paging.*
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Movement
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.MovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovementRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : MovementRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override fun getMovements(type: String?, productId: Int?, warehouseId: Int?): Flow<PagingData<Movement>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false)
        ) {
            MovementPagingSource(api, type, productId, warehouseId)
        }.flow
    }

    override suspend fun receipt(productId: Int, warehouseId: Int, quantity: Int, referenceNumber: String?, notes: String?): Result<Movement> = safeApiCall {
        api.receipt(
            com.inventory.system.data.remote.dto.ReceiptRequest(productId, warehouseId, quantity, referenceNumber, notes)
        ).toDomain()
    }

    override suspend fun issue(productId: Int, warehouseId: Int, quantity: Int, referenceNumber: String?): Result<Movement> = safeApiCall {
        api.issue(
            com.inventory.system.data.remote.dto.IssueRequest(productId, warehouseId, quantity, referenceNumber)
        ).toDomain()
    }

    override suspend fun transfer(productId: Int, fromWarehouseId: Int, toWarehouseId: Int, quantity: Int): Result<Movement> = safeApiCall {
        api.transfer(
            com.inventory.system.data.remote.dto.TransferRequest(productId, fromWarehouseId, toWarehouseId, quantity)
        ).toDomain()
    }
}

class MovementPagingSource(
    private val api: InventoryApiService,
    private val type: String?,
    private val productId: Int?,
    private val warehouseId: Int?
) : PagingSource<Int, Movement>() {

    override fun getRefreshKey(state: PagingState<Int, Movement>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movement> {
        val page = params.key ?: 1
        return try {
            val response = api.getMovements(type, productId, warehouseId, page = page, size = params.loadSize)
            LoadResult.Page(
                data = response.items.map { it.toDomain() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

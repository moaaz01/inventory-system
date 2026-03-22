package com.inventory.system.data.repository

import androidx.paging.*
import com.inventory.system.data.local.dao.ProductDao
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Product
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val productDao: ProductDao
) : ProductRepository {

    override fun getProducts(search: String?, categoryId: Int?): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false)
        ) {
            ProductPagingSource(api, search, categoryId)
        }.flow
    }

    override suspend fun getProduct(id: Int): Result<Product> = safeApiCall {
        api.getProduct(id).toDomain()
    }

    override suspend fun createProduct(sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int): Result<Product> = safeApiCall {
        val dto = api.createProduct(
            com.inventory.system.data.remote.dto.ProductCreateRequest(sku, name, categoryId, unitId, minStockLevel)
        )
        productDao.upsert(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun updateProduct(id: Int, sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int): Result<Product> = safeApiCall {
        val dto = api.updateProduct(
            id, com.inventory.system.data.remote.dto.ProductCreateRequest(sku, name, categoryId, unitId, minStockLevel)
        )
        productDao.upsert(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun deleteProduct(id: Int): Result<Unit> = safeApiCall {
        api.deleteProduct(id)
    }

    override suspend fun fetchAllProducts(): Result<List<Product>> = safeApiCall {
        val response = api.getAllProducts(page = 1, size = 9999)
        val products = response.items.map { it.toDomain() }
        productDao.upsertAll(response.items.map { it.toEntity() })
        products
    }

    override suspend fun getProductBySku(sku: String): Product? {
        return productDao.getProductBySku(sku)?.toDomain()
    }

    override fun getCachedProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }
}

class ProductPagingSource(
    private val api: InventoryApiService,
    private val search: String?,
    private val categoryId: Int?
) : PagingSource<Int, Product>() {

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) ?: state.closestPageToPosition(it)?.nextKey?.minus(1) }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val page = params.key ?: 1
        return try {
            val response = api.getProducts(search, categoryId, page, params.loadSize)
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

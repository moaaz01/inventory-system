package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.*
import com.inventory.system.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : ReportRepository {

    override suspend fun getInventoryReport(): Result<List<InventoryReportItem>> = safeApiCall {
        api.getInventoryReport().map { it.toDomain() }
    }

    override suspend fun getLowStockReport(): Result<List<Product>> = safeApiCall {
        api.getLowStockReport().map { it.toDomain() }
    }

    override suspend fun getMovementsReport(fromDate: String?, toDate: String?, movementType: String?): Result<List<Movement>> = safeApiCall {
        api.getMovementsReport(fromDate, toDate, movementType).map { it.toDomain() }
    }

    override suspend fun getDashboardStats(): Result<DashboardStats> = safeApiCall {
        api.getDashboardStats().toDomain()
    }
}

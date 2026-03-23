package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.*
import com.inventory.system.domain.repository.ReportRepository
import com.inventory.system.util.DebugLogger
import javax.inject.Inject

private const val TAG = "ReportRepository"

class ReportRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : ReportRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getInventoryReport(): Result<List<InventoryReportItem>> {
        DebugLogger.logApiCall(TAG, "/api/reports/inventory")
        return safeApiCall {
            val result = api.getInventoryReport().map { it.toDomain() }
            DebugLogger.logApiResponse(TAG, "/api/reports/inventory", true, result.size)
            DebugLogger.verifyExecution(TAG, "getInventoryReport")
            result
        }
    }

    override suspend fun getLowStockReport(): Result<List<Product>> {
        DebugLogger.logApiCall(TAG, "/api/reports/low-stock")
        return safeApiCall {
            val result = api.getLowStockReport().map { it.toDomain() }
            DebugLogger.logApiResponse(TAG, "/api/reports/low-stock", true, result.size)
            DebugLogger.verifyExecution(TAG, "getLowStockReport")
            result
        }
    }

    override suspend fun getMovementsReport(fromDate: String?, toDate: String?, movementType: String?): Result<List<Movement>> {
        DebugLogger.logApiCall(TAG, "/api/reports/movements")
        return safeApiCall {
            val result = api.getMovementsReport(fromDate, toDate, movementType).map { it.toDomain() }
            DebugLogger.logApiResponse(TAG, "/api/reports/movements", true, result.size)
            DebugLogger.verifyExecution(TAG, "getMovementsReport")
            result
        }
    }

    override suspend fun getDashboardStats(): Result<DashboardStats> {
        DebugLogger.logApiCall(TAG, "/api/dashboard/stats")
        DebugLogger.verifyExecution(TAG, "getDashboardStats called")
        return safeApiCall {
            val result = api.getDashboardStats().toDomain()
            DebugLogger.logApiResponse(TAG, "/api/dashboard/stats", true, 1)
            DebugLogger.d(TAG, "Dashboard Stats: products=${result.totalProducts}, warehouses=${result.totalWarehouses}, lowStock=${result.lowStockCount}")
            DebugLogger.verifyExecution(TAG, "getDashboardStats completed")
            result
        }
    }
}

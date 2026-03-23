package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.data.remote.dto.ImportResult
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.ExportImportRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ExportImportRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : ExportImportRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun exportProducts(): Result<ByteArray> = safeApiCall {
        api.exportProducts().bytes()
    }

    override suspend fun exportWarehouses(): Result<ByteArray> = safeApiCall {
        api.exportWarehouses().bytes()
    }

    override suspend fun exportAll(): Result<ByteArray> = safeApiCall {
        api.exportAll().bytes()
    }

    override suspend fun importProducts(fileBytes: ByteArray, fileName: String): Result<ImportResult> = safeApiCall {
        val body = fileBytes.toRequestBody("text/csv".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        api.importProducts(part)
    }

    override suspend fun importWarehouses(fileBytes: ByteArray, fileName: String): Result<ImportResult> = safeApiCall {
        val body = fileBytes.toRequestBody("text/csv".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        api.importWarehouses(part)
    }
}

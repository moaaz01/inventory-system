package com.inventory.system.presentation.exportimport

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.data.remote.dto.ImportResult
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.ExportImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ExportImportUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ExportImportViewModel @Inject constructor(
    private val repository: ExportImportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState: StateFlow<ExportImportUiState> = _uiState.asStateFlow()

    fun exportProducts(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.exportProducts()) {
                is Result.Success -> {
                    saveFile(context, "products_export.csv", r.data, "text/csv")
                    _uiState.update { it.copy(isLoading = false, successMessage = "تم تصدير المنتجات إلى مجلد التنزيلات") }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun exportWarehouses(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.exportWarehouses()) {
                is Result.Success -> {
                    saveFile(context, "warehouses_export.csv", r.data, "text/csv")
                    _uiState.update { it.copy(isLoading = false, successMessage = "تم تصدير المستودعات إلى مجلد التنزيلات") }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun exportAll(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.exportAll()) {
                is Result.Success -> {
                    saveFile(context, "inventory_export.zip", r.data, "application/zip")
                    _uiState.update { it.copy(isLoading = false, successMessage = "تم تصدير كل البيانات إلى مجلد التنزيلات") }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun importProducts(fileBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.importProducts(fileBytes, fileName)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, successMessage = "تم الاستيراد: ${r.data.count} سجل") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun importWarehouses(fileBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.importWarehouses(fileBytes, fileName)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, successMessage = "تم الاستيراد: ${r.data.count} سجل") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    private fun saveFile(context: Context, fileName: String, data: ByteArray, mimeType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os -> os.write(data) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(data) }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(successMessage = null, errorMessage = null) } }
}

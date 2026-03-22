package com.inventory.system.presentation.warehouses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Warehouse
import com.inventory.system.domain.repository.WarehouseRepository
import com.inventory.system.util.DebugLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WarehouseViewModel"

data class WarehouseUiState(
    val isLoading: Boolean = false,
    val warehouses: List<Warehouse> = emptyList(),
    val selectedWarehouse: Warehouse? = null,
    val error: String? = null,
    val actionSuccess: Boolean = false,
    val actionError: String? = null
)

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val warehouseRepository: WarehouseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WarehouseUiState())
    val uiState: StateFlow<WarehouseUiState> = _uiState.asStateFlow()

    init { 
        DebugLogger.verifyExecution(TAG, "WarehouseViewModel initialized")
        loadWarehouses() 
    }

    fun loadWarehouses() {
        DebugLogger.logUserAction(TAG, "loadWarehouses")
        viewModelScope.launch {
            _uiState.update { 
                DebugLogger.logStateChange(TAG, "isLoading", it.isLoading, true)
                it.copy(isLoading = true, error = null) 
            }
            when (val r = warehouseRepository.getWarehouses()) {
                is Result.Success -> {
                    DebugLogger.d(TAG, "✅ Warehouses loaded: ${r.data.size}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "warehouses", it.warehouses.size, r.data.size)
                        it.copy(isLoading = false, warehouses = r.data) 
                    }
                    DebugLogger.checkConnection(TAG, "WarehouseRepository", "WarehouseViewModel", true)
                }
                is Result.Error -> {
                    DebugLogger.e(TAG, "❌ Failed to load warehouses: ${r.message}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "error", it.error, r.message)
                        it.copy(isLoading = false, error = r.message) 
                    }
                    DebugLogger.checkConnection(TAG, "WarehouseRepository", "WarehouseViewModel", false)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadWarehouse(id: Int) {
        DebugLogger.logUserAction(TAG, "loadWarehouse", mapOf("id" to id))
        viewModelScope.launch {
            _uiState.update { 
                DebugLogger.logStateChange(TAG, "isLoading", it.isLoading, true)
                it.copy(isLoading = true, error = null) 
            }
            when (val r = warehouseRepository.getWarehouse(id)) {
                is Result.Success -> {
                    DebugLogger.d(TAG, "✅ Warehouse detail loaded: ${r.data.name}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "selectedWarehouse", it.selectedWarehouse, r.data)
                        it.copy(isLoading = false, selectedWarehouse = r.data) 
                    }
                }
                is Result.Error -> {
                    DebugLogger.e(TAG, "❌ Failed to load warehouse $id: ${r.message}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "error", it.error, r.message)
                        it.copy(isLoading = false, error = r.message) 
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun createWarehouse(name: String, location: String?) {
        DebugLogger.logUserAction(TAG, "createWarehouse", mapOf("name" to name))
        viewModelScope.launch {
            _uiState.update { 
                DebugLogger.logStateChange(TAG, "isLoading", it.isLoading, true)
                it.copy(isLoading = true, actionError = null) 
            }
            when (val r = warehouseRepository.createWarehouse(name, location)) {
                is Result.Success -> {
                    DebugLogger.d(TAG, "✅ Warehouse created: ${r.data.name}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "actionSuccess", it.actionSuccess, true)
                        it.copy(isLoading = false, actionSuccess = true) 
                    }
                    loadWarehouses()
                }
                is Result.Error -> {
                    DebugLogger.e(TAG, "❌ Failed to create warehouse: ${r.message}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "actionError", it.actionError, r.message)
                        it.copy(isLoading = false, actionError = r.message) 
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun updateWarehouse(id: Int, name: String, location: String?) {
        DebugLogger.logUserAction(TAG, "updateWarehouse", mapOf("id" to id, "name" to name))
        viewModelScope.launch {
            _uiState.update { 
                DebugLogger.logStateChange(TAG, "isLoading", it.isLoading, true)
                it.copy(isLoading = true, actionError = null) 
            }
            when (val r = warehouseRepository.updateWarehouse(id, name, location)) {
                is Result.Success -> {
                    DebugLogger.d(TAG, "✅ Warehouse updated: ${r.data.name}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "actionSuccess", it.actionSuccess, true)
                        it.copy(isLoading = false, actionSuccess = true) 
                    }
                    loadWarehouses()
                }
                is Result.Error -> {
                    DebugLogger.e(TAG, "❌ Failed to update warehouse: ${r.message}")
                    _uiState.update { 
                        DebugLogger.logStateChange(TAG, "actionError", it.actionError, r.message)
                        it.copy(isLoading = false, actionError = r.message) 
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearActionState() {
        _uiState.update { 
            DebugLogger.d(TAG, "Clearing action state")
            it.copy(actionSuccess = false, actionError = null) 
        }
    }
}

package com.inventory.system.presentation.warehouses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.Warehouse
import com.inventory.system.domain.repository.WarehouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    init { loadWarehouses() }

    fun loadWarehouses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = warehouseRepository.getWarehouses()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, warehouses = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun loadWarehouse(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = warehouseRepository.getWarehouse(id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, selectedWarehouse = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun createWarehouse(name: String, location: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = warehouseRepository.createWarehouse(name, location)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                    loadWarehouses()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun updateWarehouse(id: Int, name: String, location: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = warehouseRepository.updateWarehouse(id, name, location)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                    loadWarehouses()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearActionState() {
        _uiState.update { it.copy(actionSuccess = false, actionError = null) }
    }
}

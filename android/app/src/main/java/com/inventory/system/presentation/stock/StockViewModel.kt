package com.inventory.system.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.*
import com.inventory.system.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val warehouses: List<Warehouse> = emptyList(),
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val movementRepository: MovementRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.getCachedProducts().collect { prods ->
                _uiState.update { it.copy(products = prods) }
            }
        }
        viewModelScope.launch {
            warehouseRepository.getCachedWarehouses().collect { warehouses ->
                _uiState.update { it.copy(warehouses = warehouses) }
            }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val r = warehouseRepository.getWarehouses()
            if (r is Result.Success) _uiState.update { it.copy(warehouses = r.data) }
        }
        viewModelScope.launch {
            productRepository.getProducts(null, null).collect { /* populate cache */ }
        }
    }

    fun receipt(productId: Int, warehouseId: Int, quantity: Int, ref: String?, notes: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            when (val r = movementRepository.receipt(productId, warehouseId, quantity, ref, notes)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = "تم الاستلام بنجاح") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun issue(productId: Int, warehouseId: Int, quantity: Int, ref: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            when (val r = movementRepository.issue(productId, warehouseId, quantity, ref)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = "تم الصرف بنجاح") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun transfer(productId: Int, fromId: Int, toId: Int, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            when (val r = movementRepository.transfer(productId, fromId, toId, quantity)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = "تم النقل بنجاح") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(error = null, success = null) } }
}

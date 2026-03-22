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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val warehouseResult = warehouseRepository.getWarehouses()
            if (warehouseResult is Result.Success) {
                _uiState.update { it.copy(warehouses = warehouseResult.data) }
            }
            val productResult = productRepository.fetchAllProducts()
            if (productResult is Result.Success) {
                _uiState.update { it.copy(products = productResult.data, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
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

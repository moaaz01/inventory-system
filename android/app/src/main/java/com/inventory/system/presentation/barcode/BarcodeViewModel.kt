package com.inventory.system.presentation.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Product
import com.inventory.system.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeUiState(
    val isLoading: Boolean = false,
    val foundProduct: Product? = null,
    val notFound: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeUiState())
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    fun searchBySku(sku: String) {
        if (sku.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, notFound = false) }
            // Try network search first for real-time results
            val searchResult = productRepository.searchProducts(sku.trim())
            val product = when (searchResult) {
                is com.inventory.system.domain.model.Result.Success -> searchResult.data.firstOrNull()
                else -> null
            }
            if (product != null) {
                _uiState.update { it.copy(isLoading = false, foundProduct = product, notFound = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, foundProduct = null, notFound = true) }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { BarcodeUiState() }
    }
}

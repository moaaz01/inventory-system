package com.inventory.system.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventory.system.domain.model.*
import com.inventory.system.domain.model.Unit as InventoryUnit
import com.inventory.system.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val units: List<InventoryUnit> = emptyList(),
    val selectedProduct: Product? = null,
    val actionSuccess: Boolean = false,
    val actionError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)

    val products: Flow<PagingData<Product>> = combine(_searchQuery, _selectedCategoryId) { q, cat -> q to cat }
        .debounce(300)
        .flatMapLatest { (q, cat) -> productRepository.getProducts(q.ifBlank { null }, cat) }
        .cachedIn(viewModelScope)

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            categoryRepository.getCachedCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            unitRepository.getCachedUnits().collect { units ->
                _uiState.update { it.copy(units = units) }
            }
        }
        viewModelScope.launch {
            categoryRepository.getCategories() // populates cache → updates state via flow
        }
        viewModelScope.launch {
            unitRepository.getUnits() // populates cache → updates state via flow
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(categoryId: Int?) { _selectedCategoryId.value = categoryId }

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = productRepository.getProduct(id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, selectedProduct = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun createProduct(sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = productRepository.createProduct(sku, name, categoryId, unitId, minStockLevel)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun updateProduct(id: Int, sku: String, name: String, categoryId: Int?, unitId: Int?, minStockLevel: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = productRepository.updateProduct(id, sku, name, categoryId, unitId, minStockLevel)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            when (val r = productRepository.deleteProduct(id)) {
                is Result.Success -> _uiState.update { it.copy(actionSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearActionState() {
        _uiState.update { it.copy(actionSuccess = false, actionError = null) }
    }
}

package com.inventory.system.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventory.system.domain.model.*
import com.inventory.system.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = false,
    val inventoryReport: List<InventoryReportItem> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    val movements: Flow<PagingData<Movement>> = movementRepository.getMovements().cachedIn(viewModelScope)

    fun loadInventoryReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = reportRepository.getInventoryReport()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, inventoryReport = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun loadLowStockReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = reportRepository.getLowStockReport()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, lowStockProducts = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }
}

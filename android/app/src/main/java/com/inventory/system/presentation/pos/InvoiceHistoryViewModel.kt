package com.inventory.system.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Invoice
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceHistoryUiState(
    val isLoading: Boolean = false,
    val invoices: List<Invoice> = emptyList(),
    val error: String? = null,
    val search: String = "",
    val startDate: String? = null,
    val endDate: String? = null
)

@HiltViewModel
class InvoiceHistoryViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceHistoryUiState())
    val uiState: StateFlow<InvoiceHistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val s = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = invoiceRepository.getInvoices(
                search = s.search.ifBlank { null },
                startDate = s.startDate,
                endDate = s.endDate
            )
            when (result) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, invoices = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun setSearch(query: String) {
        _uiState.update { it.copy(search = query) }
        load()
    }

    fun setDateRange(start: String?, end: String?) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        load()
    }
}

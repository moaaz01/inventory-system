package com.inventory.system.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.DashboardStats
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.ReportRepository
import com.inventory.system.util.DebugLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DashboardViewModel"

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { 
        DebugLogger.verifyExecution(TAG, "DashboardViewModel initialized")
        loadStats() 
    }

    fun loadStats() {
        DebugLogger.logUserAction(TAG, "loadStats")
        DebugLogger.d(TAG, "🔄 Loading dashboard stats...")
        viewModelScope.launch {
            _uiState.update { 
                val old = it
                DebugLogger.logStateChange(TAG, "isLoading", old.isLoading, true)
                it.copy(isLoading = true, error = null) 
            }
            when (val result = reportRepository.getDashboardStats()) {
                is Result.Success -> {
                    DebugLogger.d(TAG, "✅ Dashboard stats loaded: ${result.data}")
                    _uiState.update { 
                        val old = it
                        DebugLogger.logStateChange(TAG, "stats", old.stats, result.data)
                        it.copy(isLoading = false, stats = result.data) 
                    }
                    DebugLogger.checkConnection(TAG, "ReportRepository", "DashboardViewModel", true)
                }
                is Result.Error -> {
                    DebugLogger.e(TAG, "❌ Failed to load dashboard stats: ${result.message}")
                    _uiState.update { 
                        val old = it
                        DebugLogger.logStateChange(TAG, "error", old.error, result.message)
                        it.copy(isLoading = false, error = result.message) 
                    }
                    DebugLogger.checkConnection(TAG, "ReportRepository", "DashboardViewModel", false)
                }
                is Result.Loading -> {}
            }
        }
    }
}

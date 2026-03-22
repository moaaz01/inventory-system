package com.inventory.system.presentation.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.system.domain.model.Category
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ──────────── ViewModel ────────────

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val actionSuccess: Boolean = false,
    val actionError: String? = null
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getCachedCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = categoryRepository.getCategories()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, categories = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = categoryRepository.createCategory(name)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                    loadCategories()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun updateCategory(id: Int, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = categoryRepository.updateCategory(id, name)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
                    loadCategories()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = categoryRepository.deleteCategory(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    loadCategories()
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

// ──────────── Screen ────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الفئات") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::loadCategories) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة فئة")
            }
        }
    ) { padding ->
        if (uiState.isLoading && uiState.categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::loadCategories) { Text("إعادة المحاولة") }
                }
            }
        } else if (uiState.categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد فئات", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(uiState.categories, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(cat.name, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = { editingCategory = cat }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = { deleteTarget = cat }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        uiState.actionError?.let { err ->
            LaunchedEffect(err) {
                // show snackbar or just clear
                viewModel.clearActionState()
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        CategoryDialog(
            title = "إضافة فئة",
            initialName = "",
            onConfirm = { name ->
                viewModel.createCategory(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Dialog
    editingCategory?.let { cat ->
        CategoryDialog(
            title = "تعديل الفئة",
            initialName = cat.name,
            onConfirm = { name ->
                viewModel.updateCategory(cat.id, name)
                editingCategory = null
            },
            onDismiss = { editingCategory = null }
        )
    }

    // Delete Confirm Dialog
    deleteTarget?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف الفئة") },
            text = { Text("هل تريد حذف الفئة \"${cat.name}\"؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat.id)
                    deleteTarget = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun CategoryDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("اسم الفئة") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

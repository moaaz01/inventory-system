package com.inventory.system.presentation.users

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
import com.inventory.system.domain.model.User
import com.inventory.system.presentation.components.ErrorScreen
import com.inventory.system.presentation.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { username, email, password, role ->
                showAddDialog = false
                viewModel.createUser(username, email, password, role)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("إدارة المستخدمين") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::loadUsers) { Icon(Icons.Default.Refresh, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مستخدم")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = viewModel::loadUsers, modifier = Modifier.padding(padding))
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.users) { user ->
                    UserListItem(
                        user = user,
                        onToggleRole = { viewModel.updateUserRole(user.id, if (user.role == "admin") "user" else "admin") },
                        onToggleActive = { viewModel.toggleUserActive(user.id, !user.isActive) },
                        onDelete = { viewModel.deleteUser(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    onToggleRole: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف المستخدم") },
            text = { Text("هل تريد حذف المستخدم ${user.username}؟") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(user.username, style = MaterialTheme.typography.titleSmall)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = onToggleRole,
                        label = { Text(if (user.role == "admin") "مشرف" else "مستخدم") },
                        leadingIcon = {
                            Icon(
                                if (user.role == "admin") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                null, modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    IconButton(onClick = onToggleActive, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Block,
                            null,
                            tint = if (user.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (!user.isActive) {
                Text("الحساب معطّل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, email: String, password: String, role: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مستخدم جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("اسم المستخدم") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الدور:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = role == "user", onClick = { role = "user" })
                        Text("مستخدم")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = role == "admin", onClick = { role = "admin" })
                        Text("مشرف")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(username, email, password, role) },
                enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) { Text("إضافة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

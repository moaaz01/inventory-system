package com.inventory.system.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.inventory.system.presentation.auth.LoginScreen
import com.inventory.system.presentation.auth.RegisterScreen
import com.inventory.system.presentation.auth.SplashScreen
import com.inventory.system.presentation.barcode.BarcodeScannerScreen
import com.inventory.system.presentation.dashboard.DashboardScreen
import com.inventory.system.presentation.exportimport.ExportImportScreen
import com.inventory.system.presentation.products.AddEditProductScreen
import com.inventory.system.presentation.products.ProductDetailScreen
import com.inventory.system.presentation.products.ProductsScreen
import com.inventory.system.presentation.reports.ReportsScreen
import com.inventory.system.presentation.stock.StockOperationsScreen
import com.inventory.system.presentation.categories.CategoriesScreen
import com.inventory.system.presentation.users.UsersScreen
import com.inventory.system.presentation.warehouses.AddEditWarehouseScreen
import com.inventory.system.presentation.warehouses.WarehouseDetailScreen
import com.inventory.system.presentation.warehouses.WarehousesScreen
import com.inventory.system.presentation.settings.SettingsScreen
import com.inventory.system.presentation.pos.CashierScreen
import com.inventory.system.presentation.pos.InvoiceHistoryScreen
import com.inventory.system.presentation.pos.InvoiceDetailScreen

@Composable
fun InventoryNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegistered = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                onNavigateToEditProduct = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) },
                onNavigateToWarehouseDetail = { id -> navController.navigate(Screen.WarehouseDetail.createRoute(id)) },
                onNavigateToAddWarehouse = { navController.navigate(Screen.AddEditWarehouse.createRoute()) },
                onNavigateToBarcodeScanner = { navController.navigate(Screen.BarcodeScanner.route) },
                onNavigateToCashier = { navController.navigate(Screen.Cashier.route) }
            )
        }

        composable(route = Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) }
            )
        }

        composable(route = Screen.AddEditProduct.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
            val sku = backStackEntry.arguments?.getString("sku")
            AddEditProductScreen(
                productId = productId,
                initialSku = sku,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.WarehouseDetail.route) { backStackEntry ->
            val warehouseId = backStackEntry.arguments?.getString("warehouseId")?.toIntOrNull() ?: return@composable
            WarehouseDetailScreen(
                warehouseId = warehouseId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.AddEditWarehouse.createRoute(id)) }
            )
        }

        composable(route = Screen.AddEditWarehouse.route) { backStackEntry ->
            val warehouseId = backStackEntry.arguments?.getString("warehouseId")?.toIntOrNull()
            AddEditWarehouseScreen(
                warehouseId = warehouseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Categories.route) {
            CategoriesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToUsers = { navController.navigate(Screen.Users.route) },
                onNavigateToExportImport = { navController.navigate(Screen.ExportImport.route) }
            )
        }

        composable(route = Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeScanned = { sku ->
                    navController.popBackStack()
                    navController.navigate(Screen.AddEditProductWithSku.createRouteWithSku(sku))
                },
                onProductFound = { productId ->
                    navController.popBackStack()
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onProductNotFound = { sku ->
                    navController.popBackStack()
                    navController.navigate(Screen.AddEditProductWithSku.createRouteWithSku(sku))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Users.route) {
            UsersScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.ExportImport.route) {
            ExportImportScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.Cashier.route) {
            CashierScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInvoice = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) },
                onNavigateToInvoiceHistory = { navController.navigate(Screen.InvoiceHistory.route) }
            )
        }

        composable(route = Screen.InvoiceHistory.route) {
            InvoiceHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInvoice = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) }
            )
        }

        composable(route = Screen.InvoiceDetail.route) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")?.toIntOrNull() ?: return@composable
            InvoiceDetailScreen(
                invoiceId = invoiceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Int) -> Unit,
    onNavigateToWarehouseDetail: (Int) -> Unit,
    onNavigateToAddWarehouse: () -> Unit = {},
    onNavigateToBarcodeScanner: () -> Unit = {},
    onNavigateToCashier: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.contentDesc,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                item.labelRes,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onLogout = onNavigateToLogin,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToBarcodeScanner = onNavigateToBarcodeScanner,
                    onNavigateToCashier = onNavigateToCashier
                )
            }
            composable(Screen.Products.route) {
                ProductsScreen(
                    onProductClick = onNavigateToProductDetail,
                    onAddProduct = onNavigateToAddProduct,
                    onBarcodeScanner = onNavigateToBarcodeScanner
                )
            }
            composable(Screen.Warehouses.route) {
                WarehousesScreen(
                    onWarehouseClick = onNavigateToWarehouseDetail,
                    onAddWarehouse = onNavigateToAddWarehouse
                )
            }
            composable(Screen.StockOperations.route) {
                StockOperationsScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

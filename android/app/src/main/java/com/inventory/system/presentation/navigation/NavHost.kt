package com.inventory.system.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.inventory.system.presentation.auth.LoginScreen
import com.inventory.system.presentation.auth.RegisterScreen
import com.inventory.system.presentation.auth.SplashScreen
import com.inventory.system.presentation.dashboard.DashboardScreen
import com.inventory.system.presentation.products.AddEditProductScreen
import com.inventory.system.presentation.products.ProductDetailScreen
import com.inventory.system.presentation.products.ProductsScreen
import com.inventory.system.presentation.reports.ReportsScreen
import com.inventory.system.presentation.stock.StockOperationsScreen
import com.inventory.system.presentation.categories.CategoriesScreen
import com.inventory.system.presentation.warehouses.AddEditWarehouseScreen
import com.inventory.system.presentation.warehouses.WarehouseDetailScreen
import com.inventory.system.presentation.warehouses.WarehousesScreen

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
                onNavigateToProductDetail = { id ->
                    navController.navigate(Screen.ProductDetail.createRoute(id))
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddEditProduct.createRoute())
                },
                onNavigateToEditProduct = { id ->
                    navController.navigate(Screen.AddEditProduct.createRoute(id))
                },
                onNavigateToWarehouseDetail = { id ->
                    navController.navigate(Screen.WarehouseDetail.createRoute(id))
                },
                onNavigateToAddWarehouse = {
                    navController.navigate(Screen.AddEditWarehouse.createRoute())
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) }
            )
        }

        composable(route = Screen.AddEditProduct.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
            AddEditProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.WarehouseDetail.route) { backStackEntry ->
            val warehouseId = backStackEntry.arguments?.getString("warehouseId")?.toIntOrNull() ?: return@composable
            WarehouseDetailScreen(
                warehouseId = warehouseId,
                onNavigateBack = { navController.popBackStack() }
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
    }
}

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Int) -> Unit,
    onNavigateToWarehouseDetail: (Int) -> Unit,
    onNavigateToAddWarehouse: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.labelRes) },
                        label = { Text(item.labelRes) },
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                DashboardScreen(onLogout = onNavigateToLogin)
            }
            composable(Screen.Products.route) {
                ProductsScreen(
                    onProductClick = onNavigateToProductDetail,
                    onAddProduct = onNavigateToAddProduct
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
        }
    }
}

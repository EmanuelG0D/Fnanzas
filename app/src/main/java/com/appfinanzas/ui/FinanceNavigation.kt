package com.appfinanzas.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appfinanzas.data.TransactionType

@Composable
fun FinanceAppNavigation(viewModel: DashboardViewModel, openAddExpense: Boolean) {
    val navController = rememberNavController()
    val startDest = if (openAddExpense) "transaction/EXPENSE" else "dashboard"

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = startDest) {
                composable("dashboard") { 
                    DashboardScreen(viewModel, navController) 
                }
                composable("config") { 
                    ConfigScreen(viewModel, navController) 
                }
                composable("categories") { 
                    CategoriesScreen(viewModel, navController) 
                }
                composable("history/{type}") { backStackEntry ->
                     val typeStr = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                     val historyType = try {
                         HistoryType.valueOf(typeStr)
                     } catch (e: Exception) { HistoryType.EXPENSE }
                     HistoryScreen(viewModel, navController, historyType)
                }
                composable("transaction/{type}") { backStackEntry ->
                    val typeStr = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                    val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
                    TransactionScreen(viewModel, navController, type)
                }
            }
        }
    }
}

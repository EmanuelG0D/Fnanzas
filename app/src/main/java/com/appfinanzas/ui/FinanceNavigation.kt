package com.appfinanzas.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appfinanzas.data.TransactionType

import androidx.navigation.navArgument
import androidx.navigation.NavType

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
                composable("reports") { 
                    ReportsScreen(viewModel, navController) 
                }
                composable("history/{type}") { backStackEntry ->
                     val typeStr = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                     val historyType = try {
                         HistoryType.valueOf(typeStr)
                     } catch (e: Exception) { HistoryType.EXPENSE }
                     HistoryScreen(viewModel, navController, historyType)
                }
                composable(
                    "transaction/{type}?txId={txId}&amount={amount}&catId={catId}&payId={payId}&desc={desc}&date={date}",
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType },
                        navArgument("txId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("amount") { type = NavType.FloatType; defaultValue = 0f },
                        navArgument("catId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("payId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("desc") { type = NavType.StringType; defaultValue = "" },
                        navArgument("date") { type = NavType.LongType; defaultValue = 0L }
                    )
                ) { backStackEntry ->
                    val typeStr = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                    val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
                    
                    val txId = backStackEntry.arguments?.getInt("txId") ?: -1
                    val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                    val catId = backStackEntry.arguments?.getInt("catId") ?: -1
                    val payId = backStackEntry.arguments?.getInt("payId") ?: -1
                    val desc = backStackEntry.arguments?.getString("desc") ?: ""
                    val date = backStackEntry.arguments?.getLong("date") ?: 0L
                    
                    val editData = if (txId != -1) EditTransactionData(txId, amount, catId, payId, desc, date) else null

                    TransactionScreen(viewModel, navController, type, editData)
                }
            }
        }
    }
}

data class EditTransactionData(
    val id: Int,
    val amount: Double,
    val catId: Int,
    val payId: Int,
    val desc: String,
    val date: Long
)

enum class HistoryType {
    INCOME, EXPENSE, FIXED
}

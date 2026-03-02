package com.appfinanzas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appfinanzas.ui.DashboardViewModel
import com.appfinanzas.ui.FinanceAppNavigation

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = (application as FinanceApp).repository
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Atrapa si venimos desde el botón rápido del panel superior
        val openAddExpense = intent?.getBooleanExtra("OPEN_ADD_EXPENSE", false) ?: false

        setContent {
            FinanceAppNavigation(viewModel, openAddExpense)
        }
    }
}

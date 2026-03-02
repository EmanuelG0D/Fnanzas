package com.appfinanzas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfinanzas.data.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val salaryFlow = repository.getUserSalary()
    private val fixedExpensesFlow = repository.getFixedExpenses()
    private val monthlyExpensesFlow = repository.getMonthlyExpenses()

    val dashboardState: StateFlow<DashboardState> = combine(
        salaryFlow,
        fixedExpensesFlow,
        monthlyExpensesFlow
    ) { salary, fixedExpenses, monthlyExpenses ->
        
        val totalFixedExpenses = fixedExpenses.sumOf { it.amount }
        val unpaidFixedExpenses = fixedExpenses.filter { !it.isPaidThisMonth }.sumOf { it.amount }
        val totalSpent = monthlyExpenses.sumOf { it.amount }
        
        val realAvailable = salary - totalFixedExpenses - totalSpent
        val allFixedPaid = fixedExpenses.all { it.isPaidThisMonth }

        DashboardState(
            salary = salary,
            totalFixedExpenses = totalFixedExpenses,
            realAvailable = realAvailable,
            showWarning = !allFixedPaid && totalFixedExpenses > 0,
            hasPaidItems = allFixedPaid
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )
}

data class DashboardState(
    val salary: Double = 0.0,
    val totalFixedExpenses: Double = 0.0,
    val realAvailable: Double = 0.0,
    val showWarning: Boolean = false,
    val hasPaidItems: Boolean = false
)

package com.appfinanzas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfinanzas.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: FinanceRepository) : ViewModel() {

    init {
        initDefaults()
    }

    private fun initDefaults() {
        viewModelScope.launch {
            val methods = repository.getPaymentMethods().first()
            if (methods.isEmpty()) {
                repository.addPaymentMethod(PaymentMethod(name = "Efectivo", isCustom = false))
                repository.addPaymentMethod(PaymentMethod(name = "Nequi", isCustom = false))
                repository.addPaymentMethod(PaymentMethod(name = "Bancolombia", isCustom = false))
            }
            val expCats = repository.getCategories(TransactionType.EXPENSE).first()
            if (expCats.isEmpty()) {
                repository.addCategory(Category(name = "Comida \uD83C\uDF54", type = TransactionType.EXPENSE))
                repository.addCategory(Category(name = "Transporte \uD83D\uDE8C", type = TransactionType.EXPENSE))
                repository.addCategory(Category(name = "Ocio \uD83C\uDF7B", type = TransactionType.EXPENSE))
            }
            val incCats = repository.getCategories(TransactionType.INCOME).first()
            if (incCats.isEmpty()) {
                repository.addCategory(Category(name = "Salario \uD83D\uDCBC", type = TransactionType.INCOME))
                repository.addCategory(Category(name = "Inversión \uD83D\uDCC8", type = TransactionType.INCOME))
            }
        }
    }

    val salaryFlow = repository.getUserSalary().stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    val fixedExpenses = repository.getFixedExpenses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val expenses = repository.getMonthlyExpenses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val incomes = repository.getMonthlyIncomes().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val paymentMethods = repository.getPaymentMethods().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val expenseCats = repository.getCategories(TransactionType.EXPENSE).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val incomeCats = repository.getCategories(TransactionType.INCOME).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dashboardState = combine(salaryFlow, fixedExpenses, expenses, incomes) { sal, fixed, exp, inc ->
        val totalFixed = fixed.sumOf { it.amount }
        val totalExp = exp.sumOf { it.amount }
        val totalInc = inc.sumOf { it.amount }
        val unpaidFixed = fixed.filter { !it.isPaidThisMonth }.sumOf { it.amount }
        
        // Sumamos al salario lo extra que haya ingresado
        val realAvailable = sal + totalInc - totalFixed - totalExp

        DashboardState(
            salary = sal,
            totalFixedExpenses = totalFixed,
            realAvailable = realAvailable,
            showWarning = unpaidFixed > 0 && totalFixed > 0,
            hasPaidItems = fixed.isNotEmpty() && fixed.all { it.isPaidThisMonth }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardState())

    fun saveSalary(s: Double) = viewModelScope.launch { repository.saveUserSalary(s) }
    
    fun addFixedExpense(name: String, amount: Double) = viewModelScope.launch { 
        repository.addFixedExpense(FixedExpense(name = name, amount = amount)) 
    }
    
    fun deleteFixedExpense(id: Int) = viewModelScope.launch { repository.deleteFixedExpense(id) }
    
    fun toggleFixedExpense(id: Int, paid: Boolean) = viewModelScope.launch { repository.updateFixedExpenseStatus(id, paid) }
    
    fun addTransaction(amount: Double, type: TransactionType, catId: Int, methodId: Int, desc: String) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(amount = amount, type = type, categoryId = catId, paymentMethodId = methodId, description = desc)
            )
        }
    }

    suspend fun addCustomCategory(name: String, type: TransactionType): Int {
        return repository.addCategory(Category(name = name, type = type, isCustom = true)).toInt()
    }

    suspend fun addCustomMethod(name: String): Int {
        return repository.addPaymentMethod(PaymentMethod(name = name, isCustom = true)).toInt()
    }
}

data class DashboardState(
    val salary: Double = 0.0, 
    val totalFixedExpenses: Double = 0.0, 
    val realAvailable: Double = 0.0, 
    val showWarning: Boolean = false, 
    val hasPaidItems: Boolean = false
)

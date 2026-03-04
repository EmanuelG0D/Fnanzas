package com.appfinanzas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class FinanceRepository(private val financeDao: FinanceDao, private val context: Context) {

    private val SALARY_KEY = doublePreferencesKey("user_salary")

    fun getUserSalary(): Flow<Double> {
        return context.dataStore.data.map { preferences ->
            preferences[SALARY_KEY] ?: 0.0
        }
    }

    suspend fun saveUserSalary(salary: Double) {
        context.dataStore.edit { preferences ->
            preferences[SALARY_KEY] = salary
        }
    }

    fun getFixedExpenses(): Flow<List<FixedExpense>> = financeDao.getFixedExpenses()

    fun getMonthlyExpenses(): Flow<List<Transaction>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return financeDao.getMonthlyTransactions(TransactionType.EXPENSE, cal.timeInMillis)
    }

    fun getMonthlyIncomes(): Flow<List<Transaction>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return financeDao.getMonthlyTransactions(TransactionType.INCOME, cal.timeInMillis)
    }

    fun getCategories(type: TransactionType) = financeDao.getCategories(type)
    
    fun getPaymentMethods() = financeDao.getPaymentMethods()

    suspend fun addTransaction(transaction: Transaction) = financeDao.insertTransaction(transaction)
    
    suspend fun addPaymentMethod(method: PaymentMethod): Long = financeDao.insertPaymentMethod(method)
    
    suspend fun addCategory(category: Category): Long = financeDao.insertCategory(category)
    
    suspend fun deleteCategory(category: Category) = financeDao.deleteCategory(category)
    
    suspend fun updateCategory(category: Category) = financeDao.updateCategory(category)

    suspend fun addFixedExpense(expense: FixedExpense) = financeDao.insertFixedExpense(expense)
    
    suspend fun deleteFixedExpense(id: Int) = financeDao.deleteFixedExpense(id)
    
    suspend fun updateFixedExpenseStatus(id: Int, paid: Boolean) = financeDao.updateFixedExpensePaidStatus(id, paid)
    
    suspend fun updateFixedExpenseInstallment(id: Int, paid: Boolean, count: Int) = financeDao.updateFixedExpenseInstallment(id, paid, count)

    suspend fun updateTransaction(transaction: Transaction) = financeDao.updateTransaction(transaction)

    suspend fun deleteTransaction(id: Int) = financeDao.deleteTransaction(id)
    
    suspend fun getReportData(): List<Transaction> = financeDao.getAllTransactionsForReport()
    
    suspend fun resetAllData() {
        financeDao.deleteAllTransactions()
        financeDao.deleteAllFixedExpenses()
        // No borramos categorías para no romper la app, o las borramos si quieres full clean
        financeDao.deleteAllCategories()
    }
}

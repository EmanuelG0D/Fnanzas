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
            preferences[SALARY_KEY] ?: 1850000.0 // Valor por defecto para pruebas
        }
    }

    suspend fun saveUserSalary(salary: Double) {
        context.dataStore.edit { preferences ->
            preferences[SALARY_KEY] = salary
        }
    }

    fun getFixedExpenses(): Flow<List<FixedExpense>> {
        return financeDao.getFixedExpenses()
    }

    fun getMonthlyExpenses(): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        return financeDao.getMonthlyTransactions(TransactionType.EXPENSE, startOfMonth)
    }
}

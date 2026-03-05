package com.appfinanzas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM fixed_expenses")
    fun getFixedExpenses(): Flow<List<FixedExpense>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date >= :startDate")
    fun getMonthlyTransactions(type: TransactionType, startDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategories(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM payment_methods")
    fun getPaymentMethods(): Flow<List<PaymentMethod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethod): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedExpense(expense: FixedExpense)

    @Query("DELETE FROM fixed_expenses WHERE id = :id")
    suspend fun deleteFixedExpense(id: Int)

    @Query("UPDATE fixed_expenses SET isPaidThisMonth = :paid WHERE id = :id")
    suspend fun updateFixedExpensePaidStatus(id: Int, paid: Boolean)

    @Query("UPDATE fixed_expenses SET isPaidThisMonth = :paid, paidInstallments = :newCount WHERE id = :id")
    suspend fun updateFixedExpenseInstallment(id: Int, paid: Boolean, newCount: Int)

    @Update
    suspend fun updateTransaction(transaction: Transaction)


    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsForReport(): List<Transaction>
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
    
    @Query("DELETE FROM fixed_expenses")
    suspend fun deleteAllFixedExpenses()
    
    @Query("SELECT * FROM goals")
    fun getGoals(): Flow<List<Goal>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}

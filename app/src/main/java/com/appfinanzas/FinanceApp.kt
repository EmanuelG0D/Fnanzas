package com.appfinanzas

import android.app.Application
import com.appfinanzas.data.AppDatabase
import com.appfinanzas.data.FinanceRepository

class FinanceApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { FinanceRepository(database.financeDao(), this) }
}

package com.appfinanzas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appfinanzas.data.FixedExpense
import com.appfinanzas.data.TransactionType

enum class HistoryType {
    INCOME, EXPENSE, FIXED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: DashboardViewModel, navController: NavController, type: HistoryType) {
    val state by viewModel.dashboardState.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()

    val title = when (type) {
        HistoryType.INCOME -> "Mis Ingresos"
        HistoryType.EXPENSE -> "Mis Gastos Extras"
        HistoryType.FIXED -> "Gastos Fijos"
    }
    
    val color = when (type) {
        HistoryType.INCOME -> Color(0xFF10B981)
        HistoryType.EXPENSE -> Color(0xFFEF4444)
        HistoryType.FIXED -> Color(0xFF3B82F6) // Azul
    }

    // Filtrar transacciones para Income/Expense
    val transactionsList = if (type != HistoryType.FIXED) {
        state.recentTransactions.filter { 
            if (type == HistoryType.INCOME) it.type == TransactionType.INCOME 
            else it.type == TransactionType.EXPENSE
        }
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = color)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (type == HistoryType.FIXED) {
                // Lista de Gastos Fijos (diferente UI porque se pueden marcar/desmarcar)
                if (fixedExpenses.isEmpty()) {
                    EmptyState("No tienes gastos fijos registrados.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(fixedExpenses) { expense ->
                            FixedExpenseItem(expense, viewModel)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            } else {
                // Lista de Transacciones (Ingresos o Gastos)
                if (transactionsList.isEmpty()) {
                    EmptyState("No hay movimientos registrados.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(transactionsList) { t ->
                            TransactionItem(t)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FixedExpenseItem(expense: FixedExpense, viewModel: DashboardViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (expense.isPaidThisMonth) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expense.isPaidThisMonth) Icons.Rounded.CheckCircle else Icons.Default.Delete, // Icono condicional
                    contentDescription = null,
                    tint = if (expense.isPaidThisMonth) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                
                // Mostrar cuotas con detalle mas claro
                val subtitle = if (expense.totalInstallments > 0) {
                    val current = if (expense.isPaidThisMonth) expense.paidInstallments else expense.paidInstallments + 1
                    val statusText = if (expense.isPaidThisMonth) "pagada" else "pendiente"
                    "Cuota $current de ${expense.totalInstallments} ($statusText)"
                } else {
                    "Pago mensual recurrente"
                }
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (expense.isPaidThisMonth && expense.totalInstallments > 0) Color(0xFF10B981) else Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(expense.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Checkbox o Botón para pagar
                if (!expense.isPaidThisMonth) {
                    Button(
                        onClick = { viewModel.toggleFixedExpense(expense.id, true) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Pagar", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Pagado",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    // Opción para deshacer (si te equivocaste)
                    TextButton(
                        onClick = { viewModel.toggleFixedExpense(expense.id, false) },
                        modifier = Modifier.height(24.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Deshacer", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg, color = Color.Gray)
    }
}

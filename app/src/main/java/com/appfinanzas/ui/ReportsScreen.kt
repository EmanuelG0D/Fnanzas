package com.appfinanzas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appfinanzas.data.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: DashboardViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    
    // Tab state: 0 = Expenses, 1 = Income
    var selectedTab by remember { mutableStateOf(0) }
    
    // --- Data Processing for Expenses ---
    val variableExpenses = state.recentTransactions.filter { it.type == TransactionType.EXPENSE }
    val paidFixedExpenses = state.totalFixedExpenses - state.unpaidFixedExpenses
    
    val expenseCategories = variableExpenses.groupBy { it.categoryName }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toMutableMap()
        
    if (paidFixedExpenses > 0) {
        expenseCategories["Gastos Fijos"] = paidFixedExpenses
    }
    
    val expenseChartData = expenseCategories.toList().sortedByDescending { it.second }
    val totalExpensesForChart = expenseChartData.sumOf { it.second }

    // --- Data Processing for Income ---
    val incomeTransactions = state.recentTransactions.filter { it.type == TransactionType.INCOME }
    // Add salary as a category, or transaction if it's logged there.
    // DashboardState totalIncomes usually sums up income transactions + salary? Wait.
    // Let's check DashboardViewModel.
    // realAvailable = sal + totalInc - totalExp - paidFixed
    // So Salary is separate. We should include Salary in Income Chart if relevant.
    // Usually users want to see "Salario" vs "Otros Ingresos".
    
    val incomeCategories = incomeTransactions.groupBy { it.categoryName }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toMutableMap()
        
    if (state.salary > 0) {
        // If salary is not 0, we treat it as an Income Source
        // But do we double count if they added a transaction for Salary?
        // The app design seems to separate "Config Salary" from "Income Transactions".
        val currentSalary = incomeCategories["Salario"] ?: 0.0
        // If they haven't explicitly logged salary as a transaction, 
        // we might just show the base salary from extended config? 
        // For simplicity, let's stick to transactions + base salary if meaningful.
        // Actually, let's include base salary as "Salario Base" entry.
        incomeCategories["Salario Base"] = state.salary
    }

    val incomeChartData = incomeCategories.toList().sortedByDescending { it.second }
    val totalIncomeForChart = incomeChartData.sumOf { it.second }


    // Current Chart Data based on Tab
    val currentChartData = if (selectedTab == 0) expenseChartData else incomeChartData
    val currentTotal = if (selectedTab == 0) totalExpensesForChart else totalIncomeForChart
    val chartTitle = if (selectedTab == 0) "Distribución de Gastos" else "Fuentes de Ingreso"

    // Colors for chart
    val colors = listOf(
        Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF10B981), 
        Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFF6366F1), Color(0xFF14B8A6)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte Mensual", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Gastos") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Ingresos") })
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Summary Cards Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Total Ingresos", totalIncomeForChart, Color(0xFF10B981), Modifier.weight(1f))
                    SummaryCard("Total Gastos", totalExpensesForChart, Color(0xFFEF4444), Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(chartTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(24.dp))
                
                if (currentTotal > 0) {
                    // Pie Chart Container
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        Canvas(modifier = Modifier.size(220.dp).padding(20.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 50f
                            
                            currentChartData.forEachIndexed { index, (cat, amount) ->
                                val sweepAngle = (amount / currentTotal * 360).toFloat()
                                val color = colors[index % colors.size]
                                
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        Text(
                            text = formatMoney(currentTotal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Legend List
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(currentChartData) { (cat, amount) ->
                            val index = currentChartData.indexOfFirst { it.first == cat }
                            val color = colors[index % colors.size]
                            val percentage = (amount / currentTotal * 100).toInt()
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cat, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                Text("$percentage%", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(formatMoney(amount), fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.2f))
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay datos registrados.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(formatMoney(amount), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

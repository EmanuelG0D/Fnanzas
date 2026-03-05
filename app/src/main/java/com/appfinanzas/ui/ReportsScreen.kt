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
    
    // Process data for Chart
    val expenses = state.recentTransactions.filter { it.type == TransactionType.EXPENSE }
    val totalExpenses = expenses.sumOf { it.amount }
    
    val categoryTotals = expenses.groupBy { it.categoryName }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

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
                .padding(16.dp)
        ) {
            // Summary Cards Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Ingresos", state.totalIncomes, Color(0xFF10B981), Modifier.weight(1f))
                SummaryCard("Gastos Totales", state.totalExpenses, Color(0xFFEF4444), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Distribución de Gastos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(24.dp))
            
            if (totalExpenses > 0) {
                // Pie Chart Container
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Canvas(modifier = Modifier.size(220.dp).padding(20.dp)) {
                        var startAngle = -90f
                        val strokeWidth = 50f
                        
                        categoryTotals.forEachIndexed { index, (cat, amount) ->
                            val sweepAngle = (amount / totalExpenses * 360).toFloat()
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
                        text = formatMoney(totalExpenses),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Legend List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(categoryTotals) { (cat, amount) ->
                        val index = categoryTotals.indexOfFirst { it.first == cat }
                        val color = colors[index % colors.size]
                        val percentage = (amount / totalExpenses * 100).toInt()
                        
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
                    Text("No hay gastos registrados para mostrar.", color = Color.Gray)
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

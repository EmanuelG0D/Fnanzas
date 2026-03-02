package com.appfinanzas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen Mensual", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("config") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.salary == 0.0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = { navController.navigate("config") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("¡Aún no configuras tu sueldo!", fontWeight = FontWeight.Bold)
                        Text("Toca aquí o en el engranaje arriba para configurarlo.", fontSize = 14.sp)
                    }
                }
            }

            BalanceCard("Plata Real Disponible", state.realAvailable, true)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (state.showWarning) {
                WarningAlert(state.salary, state.totalFixedExpenses)
            } else if (state.hasPaidItems && state.totalFixedExpenses > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¡Excelente parce! Ya pagó todos los gastos fijos del mes. De aquí en adelante lo que gaste es plata libre.",
                        color = Color(0xFF2E7D32),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navController.navigate("transaction/EXPENSE") },
                    modifier = Modifier.weight(1f).height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salió Plata", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { navController.navigate("transaction/INCOME") },
                    modifier = Modifier.weight(1f).height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entró Plata", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BalanceCard(title: String, amount: Double, isHighlight: Boolean) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp).fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatter.format(amount),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (amount < 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun WarningAlert(salary: Double, fixedExpenses: Double) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Bueno parce, usted gana ${formatter.format(salary)} pero debe pagar ${formatter.format(fixedExpenses)}, así que no se gaste la plata hasta pagar primero.",
            color = Color(0xFFE65100),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

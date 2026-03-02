package com.appfinanzas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.dashboardState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen Mensual",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BalanceCard(
            title = "Plata Real Disponible",
            amount = state.realAvailable,
            isHighlight = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.showWarning) {
            WarningAlert(
                salary = state.salary,
                fixedExpenses = state.totalFixedExpenses
            )
        } else if (state.totalFixedExpenses == 0.0) {
            // Placeholder si no hay gastos
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Añade aquí tus gastos fijos para calcular la plata real.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Navegar a flujo de crear gasto */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Salió Plata \uD83D\uDCB8")
            }

            Button(
                onClick = { /* Navegar a flujo de crear ingreso */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Text("Entró Plata \uD83D\uDCB0")
            }
        }
    }
}

@Composable
fun BalanceCard(title: String, amount: Double, isHighlight: Boolean) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 16.sp)
            Text(
                text = formatter.format(amount),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (amount < 0) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun WarningAlert(salary: Double, fixedExpenses: Double) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Bueno parce, usted gana ${formatter.format(salary)} pero debe pagar ${formatter.format(fixedExpenses)}, así que no se gaste la plata hasta pagar primero.",
            color = Color(0xFFE65100),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

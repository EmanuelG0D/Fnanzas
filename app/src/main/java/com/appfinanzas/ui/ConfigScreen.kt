package com.appfinanzas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: DashboardViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()
    
    var salaryInput by remember { mutableStateOf(state.salary.toLong().toString()) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExpenseDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Gasto Fijo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sección Sueldo
            Text("Tu Sueldo Base Mensual", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = salaryInput,
                onValueChange = { salaryInput = it },
                label = { Text("Monto (ej. 1850000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val salaryValue = salaryInput.toDoubleOrNull() ?: 0.0
                    viewModel.saveSalary(salaryValue)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar Sueldo")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Sección Gastos Fijos
            Text("Tus Gastos Fijos (Este Mes)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Marca la casilla cuando ya los hayas pagado.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

            LazyColumn {
                items(fixedExpenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = expense.isPaidThisMonth,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleFixedExpense(expense.id, isChecked)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.name, fontWeight = FontWeight.SemiBold)
                                Text(formatter.format(expense.amount), color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteFixedExpense(expense.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        var expenseName by remember { mutableStateOf("") }
        var expenseAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Añadir Gasto Fijo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = expenseName,
                        onValueChange = { expenseName = it },
                        label = { Text("Nombre (ej. Arriendo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                    if (expenseName.isNotBlank() && amount > 0) {
                        viewModel.addFixedExpense(expenseName, amount)
                        showAddExpenseDialog = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

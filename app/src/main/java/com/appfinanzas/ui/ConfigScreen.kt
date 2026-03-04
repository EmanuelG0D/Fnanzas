package com.appfinanzas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: DashboardViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()
    
    // Inicializar el input sin decimales
    var salaryInput by remember(state.salary) { mutableStateOf(state.salary.toLong().toString().takeIf { it != "0" } ?: "") }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    
    // Variables para el nuevo gasto
    var newExpenseName by remember { mutableStateOf("") }
    var newExpenseAmount by remember { mutableStateOf("") }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentsCount by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate("categories") }) {
                        Text("Categorías", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gasto Fijo", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Sección Sueldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tu Sueldo Base Mensual", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = { newValue -> 
                            salaryInput = newValue.filter { it.isDigit() } 
                        },
                        label = { Text("Monto (ej. 1.850.000)") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val salaryValue = salaryInput.toDoubleOrNull() ?: 0.0
                            viewModel.saveSalary(salaryValue)
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Sueldo actualizado exitosamente ✅",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.End).height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Sueldo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sección Gastos Fijos
            Text("Tus Gastos Fijos (Este Mes)", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Marca la casilla cuando ya los hayas pagado.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            if (fixedExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes gastos fijos agregados.", color = MaterialTheme.colorScheme.outline)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(fixedExpenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = expense.isPaidThisMonth,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleFixedExpense(expense.id, isChecked)
                                    if(isChecked) {
                                        scope.launch { snackbarHostState.showSnackbar("¡Excelente! Un gasto menos. 🎉", duration = SnackbarDuration.Short) }
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(expense.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(formatMoney(expense.amount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                if (expense.totalInstallments > 0) {
                                    Text(
                                        "Cuota ${expense.paidInstallments}/${expense.totalInstallments}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                            }
                            IconButton(onClick = { 
                                viewModel.deleteFixedExpense(expense.id)
                                scope.launch { snackbarHostState.showSnackbar("Gasto fijo eliminado", duration = SnackbarDuration.Short) }
                            }) {
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
        // Cuotas
        var isInstallment by remember { mutableStateOf(false) }
        var installmentsCount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Añadir Gasto Fijo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = expenseName,
                        onValueChange = { expenseName = it },
                        label = { Text("Nombre (ej. Arriendo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { newValue -> expenseAmount = newValue.filter { it.isDigit() } },
                        label = { Text("Monto") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Check de cuotas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { isInstallment = !isInstallment }
                    ) {
                        Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                        Text("¿Es una compra a cuotas?")
                    }
                    if (isInstallment) {
                        OutlinedTextField(
                            value = installmentsCount,
                            onValueChange = { installmentsCount = it.filter { c -> c.isDigit() } },
                            label = { Text("Número de cuotas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Se borrará automáticamente al completar.", 
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                    val installments = if (isInstallment) installmentsCount.toIntOrNull() ?: 0 else 0
                    
                    if (expenseName.isNotBlank() && amount > 0) {
                        viewModel.addFixedExpense(expenseName, amount, installments)
                        showAddExpenseDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Gasto fijo agregado", duration = SnackbarDuration.Short) }
                    }
                }) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

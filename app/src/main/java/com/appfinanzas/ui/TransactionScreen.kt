package com.appfinanzas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appfinanzas.data.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(viewModel: DashboardViewModel, navController: NavController, type: TransactionType) {
    val scope = rememberCoroutineScope()
    val isExpense = type == TransactionType.EXPENSE
    val titleStr = if (isExpense) "Registrar Gasto" else "Registrar Ingreso"
    val mainColor = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf<Int?>(null) }
    var selectedMethodId by remember { mutableStateOf<Int?>(null) }

    val categories by (if (isExpense) viewModel.expenseCats else viewModel.incomeCats).collectAsState()
    val methods by viewModel.paymentMethods.collectAsState()

    var showAddCatDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleStr, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = mainColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Amount
            Text("¿Cuánta plata fue?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Valor en pesos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text("¿En qué categoría?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { selectedCatId = cat.id },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = mainColor.copy(alpha = 0.2f),
                            selectedLabelColor = mainColor
                        )
                    )
                }
                item {
                    ElevatedFilterChip(
                        selected = false,
                        onClick = { showAddCatDialog = true },
                        label = { Row { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)); Text(" Otro") } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Method Selection
            Text("Medio de Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(methods) { method ->
                    FilterChip(
                        selected = selectedMethodId == method.id,
                        onClick = { selectedMethodId = method.id },
                        label = { Text(method.name) }
                    )
                }
                item {
                    ElevatedFilterChip(
                        selected = false,
                        onClick = { showAddMethodDialog = true },
                        label = { Row { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)); Text(" Otro") } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text("Descripción (Opcional)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Ej. Almuerzo con amigos") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && selectedCatId != null && selectedMethodId != null) {
                        viewModel.addTransaction(amountValue, type, selectedCatId!!, selectedMethodId!!, description)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                enabled = amount.isNotBlank() && selectedCatId != null && selectedMethodId != null
            ) {
                Text("Guardar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    // Dialog for Custom Category
    if (showAddCatDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCatDialog = false },
            title = { Text("Nueva Categoría") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCatName.isNotBlank()) {
                        scope.launch {
                            val id = viewModel.addCustomCategory(newCatName, type)
                            selectedCatId = id
                            showAddCatDialog = false
                        }
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCatDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog for Custom Method
    if (showAddMethodDialog) {
        var newMethodName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMethodDialog = false },
            title = { Text("Nuevo Medio de Pago") },
            text = {
                OutlinedTextField(
                    value = newMethodName,
                    onValueChange = { newMethodName = it },
                    label = { Text("Nombre (Ej. Sistecredito)") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newMethodName.isNotBlank()) {
                        scope.launch {
                            val id = viewModel.addCustomMethod(newMethodName)
                            selectedMethodId = id
                            showAddMethodDialog = false
                        }
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMethodDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

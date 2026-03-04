package com.appfinanzas.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appfinanzas.data.TransactionType
import kotlinx.coroutines.launch

import android.app.DatePickerDialog
import android.widget.DatePicker
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: DashboardViewModel, 
    navController: NavController, 
    type: TransactionType,
    editData: EditTransactionData? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isExpense = type == TransactionType.EXPENSE
    val isEdit = editData != null
    val titleStr = if (isEdit) "Editar Movimiento" else if (isExpense) "Registrar Gasto" else "Registrar Ingreso"
    val mainColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)

    // Inicializar estados con datos de edición si existen
    var amount by remember { mutableStateOf(editData?.amount?.toLong()?.toString() ?: "") }
    var description by remember { mutableStateOf(editData?.desc ?: "") }
    var selectedCatId by remember { mutableStateOf<Int?>(editData?.catId?.takeIf { it != -1 }) }
    var selectedMethodId by remember { mutableStateOf<Int?>(editData?.payId?.takeIf { it != -1 }) }
    var selectedDate by remember { mutableStateOf(editData?.date ?: System.currentTimeMillis()) }

    val categories by (if (isExpense) viewModel.expenseCats else viewModel.incomeCats).collectAsState()
    val methods by viewModel.paymentMethods.collectAsState()

    var showAddCatDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    
    // Date Picker Logic
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, dayOfMonth)
            selectedDate = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(titleStr, color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
        ) {
            // Amount
            Text("¿Cuánta plata fue?", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue -> amount = newValue.filter { it.isDigit() } },
                label = { Text("Monto en pesos") },
                leadingIcon = { Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = mainColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandsSeparatorVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Black, color = mainColor),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Date Picker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                     Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                     val dateStr = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault()).format(Date(selectedDate))
                     Text(dateStr, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Selection
            Text("¿En qué categoría?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { selectedCatId = cat.id },
                        label = { Text(cat.name, fontSize = 15.sp, modifier = Modifier.padding(vertical = 8.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = mainColor.copy(alpha = 0.15f),
                            selectedLabelColor = mainColor
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                item {
                    ElevatedFilterChip(
                        selected = false,
                        onClick = { showAddCatDialog = true },
                        label = { Row(modifier = Modifier.padding(vertical = 8.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Nuevo", fontSize = 15.sp) } },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Payment Method Selection
            Text("¿De dónde salió la plata?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(methods) { method ->
                    FilterChip(
                        selected = selectedMethodId == method.id,
                        onClick = { selectedMethodId = method.id },
                        label = { Text(method.name, fontSize = 15.sp, modifier = Modifier.padding(vertical = 8.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF1D4ED8)
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                item {
                    ElevatedFilterChip(
                        selected = false,
                        onClick = { showAddMethodDialog = true },
                        label = { Row(modifier = Modifier.padding(vertical = 8.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Nuevo", fontSize = 15.sp) } },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Description
            Text("Descripción (Opcional)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Ej. Almuerzo con amigos") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val amountValue = amount.replace(".","").toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && selectedCatId != null && selectedMethodId != null) {
                        if (isEdit) {
                            viewModel.updateTransaction(
                                editData!!.id,
                                amountValue,
                                type,
                                selectedCatId!!,
                                selectedMethodId!!,
                                description,
                                selectedDate
                            )
                        } else {
                            viewModel.addTransaction(
                                amountValue, 
                                type, 
                                selectedCatId!!, 
                                selectedMethodId!!, 
                                description, 
                                selectedDate
                            )
                        }
                        
                        keyboardController?.hide()
                        val msg = if (isEdit) "Transacción actualizada correctamente" else "Transacción registrada con éxito 🚀"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                enabled = amount.isNotBlank() && selectedCatId != null && selectedMethodId != null,
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (isEdit) "Guardar Cambios" else "Cargar Transacción", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    // Dialog for Custom Category
    if (showAddCatDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCatDialog = false },
            title = { Text("Nueva Categoría", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Nombre (ej. Mascotas \uD83D\uDC36)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newCatName.isNotBlank()) {
                        scope.launch {
                            val id = viewModel.addCustomCategory(newCatName, type)
                            selectedCatId = id
                            showAddCatDialog = false
                            Toast.makeText(context, "Categoría creada ✅", Toast.LENGTH_SHORT).show()
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
            title = { Text("Nuevo Medio de Pago", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newMethodName,
                    onValueChange = { newMethodName = it },
                    label = { Text("Nombre (Ej. Sistecredito)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newMethodName.isNotBlank()) {
                        scope.launch {
                            val id = viewModel.addCustomMethod(newMethodName)
                            selectedMethodId = id
                            showAddMethodDialog = false
                            Toast.makeText(context, "Medio de pago creado ✅", Toast.LENGTH_SHORT).show()
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

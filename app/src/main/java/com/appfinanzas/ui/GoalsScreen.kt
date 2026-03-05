package com.appfinanzas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appfinanzas.data.Goal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: DashboardViewModel, navController: NavController) {
    val goals by viewModel.goals.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<Goal?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Goal?>(null) }
    
    // Deposit Dialog State
    if (selectedGoalForDeposit != null) {
        var depositAmount by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("Abonar a la meta") },
            text = {
                Column {
                    Text("¿Cuánto quieres guardar hoy para '${selectedGoalForDeposit?.name}'?")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it.filter { char -> char.isDigit() } },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text("Esto se registrará como un gasto y se restará de tu saldo disponible.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = depositAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addSavingsToGoal(selectedGoalForDeposit!!, amount)
                        selectedGoalForDeposit = null
                    }
                }) {
                    Text("Depositar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) { Text("Cancelar") }
            }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Meta") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre (ej. Viaje)") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = target, 
                        onValueChange = { target = it.filter { c -> c.isDigit() } }, 
                        label = { Text("Meta ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val t = target.toDoubleOrNull()
                    if (name.isNotBlank() && t != null && t > 0) {
                        viewModel.addGoal(name, t, null)
                        showAddDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
    
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar meta") },
            text = { Text("¿Deseas eliminar '${showDeleteConfirm?.name}'? El dinero YA ahorrado no se devolverá a tu saldo disponible (ya que se registró como gasto).") },
            confirmButton = {
                Button(
                     onClick = {
                         viewModel.deleteGoal(showDeleteConfirm!!)
                         showDeleteConfirm = null
                     },
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Metas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Meta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tienes metas activas.", color = Color.Gray)
                        Text("¡Crea una para empezar a ahorrar!", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(goals) { goal ->
                        GoalItem(
                            goal = goal, 
                            onDeposit = { selectedGoalForDeposit = goal },
                            onDelete = { showDeleteConfirm = goal }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalItem(goal: Goal, onDeposit: () -> Unit, onDelete: () -> Unit) {
    val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val isCompleted = progress >= 1f
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onDeposit() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Color(0xFFD1FAE5) else Color(0xFFEDE9FE)), // Verde o Violeta claro
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Flag, 
                            contentDescription = null, 
                            tint = if (isCompleted) Color(0xFF10B981) else Color(0xFF8B5CF6)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        if (isCompleted) {
                            Text("¡Meta Alcanzada! 🎉", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Faltan ${formatMoney(goal.targetAmount - goal.currentAmount)}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                
                // Delete button small
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                   // Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.LightGray) 
                   // Better UX: Long press/Menu. For simplicity simple trailing logic or just Deposit on click.
                   // Let's hide delete here to avoid accidents?
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMoney(goal.currentAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8B5CF6))
                Text(formatMoney(goal.targetAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = if (isCompleted) Color(0xFF10B981) else Color(0xFF8B5CF6),
                trackColor = Color(0xFFF3F4F6)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            if (!isCompleted) {
                Button(
                    onClick = onDeposit, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F3FF), contentColor = Color(0xFF8B5CF6)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("+ Abonar Dinero")
                }
            }
        }
    }
}

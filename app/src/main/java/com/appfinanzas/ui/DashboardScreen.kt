package com.appfinanzas.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.appfinanzas.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    
    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Fondo profesional y limpio
        topBar = {
            TopAppBar(
                title = { Text("Mi Billetera", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("config") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = state.salary == 0.0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = { navController.navigate("config") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("¡Falta tu sueldo!", fontWeight = FontWeight.Bold)
                            Text("Toca aquí para ir a configuración.", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Tarjeta principal (Saldo Disponible)
            MainBalanceCard(state.realAvailable)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Título de la sección
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Resumen Financiero", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Cuadrícula 2x2 de Estadísticas (Burbujas)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatBubble(
                    title = "Ingresos",
                    amount = state.salary + state.totalIncomes,
                    icon = Icons.Rounded.TrendingUp,
                    color = Color(0xFF10B981),
                    bgColor = Color(0xFFD1FAE5),
                    modifier = Modifier.weight(1f).clickable { navController.navigate("history/INCOME") }
                )
                StatBubble(
                    title = "Gastos Extras",
                    amount = state.totalExpenses,
                    icon = Icons.Rounded.ReceiptLong,
                    color = Color(0xFFEF4444),
                    bgColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f).clickable { navController.navigate("history/EXPENSE") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección de Gastos Fijos con Barra de Progreso
            FixedExpensesCard(
                total = state.totalFixedExpenses,
                unpaid = state.unpaidFixedExpenses,
                onClick = { navController.navigate("history/FIXED") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Alerta Dinámica Coloquial
            if (state.unpaidFixedExpenses > 0 && state.totalFixedExpenses > 0) {
                WarningAlert(state.salary, state.totalFixedExpenses, state.realAvailable)
            } else if (state.totalFixedExpenses > 0) {
                SuccessAlert(state.realAvailable)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de acción flotantes e imponentes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navController.navigate("transaction/EXPENSE") },
                    modifier = Modifier.weight(1f).height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gastar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { navController.navigate("transaction/INCOME") },
                    modifier = Modifier.weight(1f).height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recibir", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sección Últimos Movimientos
            if (state.recentTransactions.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimos Movimientos", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Solo mostramos las primeras 5 en el dashboard para no saturar
                state.recentTransactions.take(5).forEach { transaction ->
                    TransactionItem(transaction)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (state.recentTransactions.size > 5) {
                    TextButton(
                         onClick = { navController.navigate("history/EXPENSE") }, 
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todo el historial", color = Color.Gray)
                    }
                }
            } else {
                Text(
                    text = "Aún no hay movimientos este mes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: RecentTransaction) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
    val icon = if (isExpense) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward
    val bgColor = if (isExpense) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = amountColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                if (!transaction.description.isNullOrBlank()) {
                    Text(
                        text = transaction.description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${formatDate(transaction.date)} • ${transaction.methodName}",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
            
            Text(
                text = (if (isExpense) "- " else "+ ") + formatMoney(transaction.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = amountColor
            )
        }
    }
}

fun formatDate(dateMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM", Locale("es", "CO"))
    return formatter.format(Date(dateMillis))
}

fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

@Composable
fun MainBalanceCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Un azul oscuro muy premium
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Un pequeño adorno de fondo
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Plata Disponible", 
                    fontSize = 16.sp, 
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedContent(
                    targetState = amount,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                        } else {
                            slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                        }
                    }
                ) { targetAmount ->
                    Text(
                        text = formatMoney(targetAmount),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = if (targetAmount < 0) Color(0xFFFF5252) else Color.White,
                        letterSpacing = (-1).sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatBubble(title: String, amount: Double, icon: ImageVector, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatMoney(amount), 
                fontSize = 16.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun FixedExpensesCard(total: Double, unpaid: Double, onClick: () -> Unit) {
    val paid = total - unpaid
    val progress = if (total > 0) (paid / total).toFloat() else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF3B82F6))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Gastos Fijos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 1f) Color(0xFF10B981) else Color(0xFF3B82F6),
                trackColor = Color(0xFFF1F5F9),
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pagado", fontSize = 12.sp, color = Color.Gray)
                    Text(formatMoney(paid), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", fontSize = 12.sp, color = Color.Gray)
                    Text(formatMoney(total), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                }
            }
        }
    }
}

@Composable
fun WarningAlert(salary: Double, fixedExpenses: Double, realAvailable: Double) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Ámbar muy claro
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color(0xFFD97706))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Atención: Gastos Fijos Pendientes",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tienes obligaciones pendientes por ${formatMoney(fixedExpenses)}. Si pagas todo ahora, tu saldo real disponible sería de ${formatMoney(realAvailable)}.",
                color = Color(0xFF92400E),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SuccessAlert(realAvailable: Double) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = Color(0xFF059669))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "¡Al día con tus obligaciones!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Has cubierto todos tus gastos fijos. Tu saldo real disponible para otros gastos o ahorro es de ${formatMoney(realAvailable)}.",
                color = Color(0xFF065F46),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

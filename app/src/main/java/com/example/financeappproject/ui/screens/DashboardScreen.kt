package com.example.financeappproject.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financeappproject.ui.components.FinanceBottomBar
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val hasRealData = sharedPrefs.getBoolean("has_real_data", false)
    val userId = sharedPrefs.getString("user_id", "") ?: ""
    
    var isFabExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    
    var fullName by remember { mutableStateOf(sharedPrefs.getString("user_name", "") ?: "") }
    val firstName = fullName.split(" ").firstOrNull() ?: fullName

    val transactions = remember { mutableStateListOf<Transaction>() }
    val alerts = remember { mutableStateListOf<BillAlert>() }

    // If no name is set, show dialog
    LaunchedEffect(Unit) {
        if (fullName.isEmpty()) {
            showNameDialog = true
        }
    }

    // Load data from Supabase (or dummy)
    LaunchedEffect(Unit) {
        if (hasRealData && userId.isNotEmpty()) {
            isLoading = true
            // Mock fetching real data
            transactions.clear()
            // In real app: fetch from API
            isLoading = false
        } else if (!hasRealData) {
            transactions.addAll(listOf(
                Transaction(1, "Grocery Store", -50.0, "Oct 24", "Paid"),
                Transaction(2, "Salary", 3000.0, "Oct 23", "Paid")
            ))
            alerts.addAll(listOf(
                BillAlert("Zuku Internet", 3500.0, "Due in 2 days"),
                BillAlert("KPLC Token", 1000.0, "Due tomorrow")
            ))
        }
    }

    if (showNameDialog) {
        var tempName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Welcome!") },
            text = {
                Column {
                    Text("What should we call you?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Your Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            fullName = tempName
                            sharedPrefs.edit().putString("user_name", tempName).apply()
                            showNameDialog = false
                        }
                    }
                ) { Text("Save") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${getGreeting()}, $firstName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "PochiWise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Surface(
                            onClick = { showMenu = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile",
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.width(180.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                                onClick = { 
                                    showMenu = false
                                    navController.navigate("profile")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = { 
                                    showMenu = false
                                    navController.navigate("settings")
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = { Icon(Icons.Default.Logout, null) },
                                onClick = { 
                                    showMenu = false
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { FinanceBottomBar(navController) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (isFabExpanded) {
                    FabOption(Icons.Default.TrendingUp, "Add Income", Color(0xFF4CAF50)) {
                        isFabExpanded = false
                        navController.navigate("add_income")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FabOption(Icons.Default.TrendingDown, "Add Expense", Color(0xFFF44336)) {
                        isFabExpanded = false
                        navController.navigate("add_expense")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FabOption(Icons.Default.MoneyOff, "Add Debt", Color(0xFFFF9800)) {
                        isFabExpanded = false
                        navController.navigate("add_debt")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { SummaryCard(transactions) }
                
                if (alerts.isNotEmpty()) {
                    item { UpcomingBillsSection(alerts) }
                }

                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet.", color = Color.Gray)
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionListItem(transaction) { 
                            transactions.remove(transaction) 
                        }
                        HorizontalDivider()
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun FabOption(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                label, 
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        FloatingActionButton(
            onClick = onClick, 
            containerColor = color, 
            contentColor = Color.White,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun SummaryCard(transactions: List<Transaction>) {
    val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val expense = transactions.filter { it.amount < 0 && it.status == "Paid" }.sumOf { abs(it.amount) }
    val debt = transactions.filter { it.status == "Due" || it.status == "Pending" }.sumOf { abs(it.amount) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Balance", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Ksh ${String.format("%.2f", income - expense)}", 
                style = MaterialTheme.typography.headlineLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Income", "Ksh ${String.format("%.0f", income)}", Color(0xFF4CAF50))
                SummaryItem("Expenses", "Ksh ${String.format("%.0f", expense)}", Color(0xFFF44336))
                SummaryItem("Debts", "Ksh ${String.format("%.0f", debt)}", Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(amount, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun UpcomingBillsSection(alerts: List<BillAlert>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Due Date Alerts", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            alerts.forEach { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(alert.name, fontWeight = FontWeight.Medium)
                        Text(alert.dueDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("Ksh ${alert.amount}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: Transaction, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(transaction.title, fontWeight = FontWeight.Medium) },
        supportingContent = { 
            Row {
                Text(transaction.date, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                val statusColor = when(transaction.status) {
                    "Paid" -> Color(0xFF4CAF50)
                    "Due" -> Color.Red
                    else -> Color(0xFFFF9800)
                }
                Text(transaction.status, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = if (transaction.amount > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val prefix = if (transaction.amount > 0) "+" else ""
                Text(
                    "$prefix Ksh ${abs(transaction.amount)}", 
                    color = color, 
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) { 
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) 
                }
            }
        }
    )
}

data class Transaction(val id: Int, val title: String, val amount: Double, val date: String, val status: String)
data class BillAlert(val name: String, val amount: Double, val dueDate: String)

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

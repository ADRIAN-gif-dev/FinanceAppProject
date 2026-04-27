package com.example.financeappproject.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financeappproject.ui.components.FinanceBottomBar
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var isFabExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val userName = "John" // This would normally come from a ViewModel or Session

    // Mock state for transactions to demonstrate deletion
    val transactions = remember {
        mutableStateListOf(
            Transaction(1, "Grocery Store", -50.0, "Oct 24"),
            Transaction(2, "Salary", 3000.0, "Oct 23"),
            Transaction(3, "Coffee Shop", -5.5, "Oct 22"),
            Transaction(4, "Rent", -500.0, "Oct 20"),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${getGreeting()}, $userName",
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
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("profile")
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("settings")
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
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
                    FabOption(
                        icon = Icons.Default.TrendingUp,
                        label = "Add Income",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            isFabExpanded = false
                            navController.navigate("add_income")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FabOption(
                        icon = Icons.Default.TrendingDown,
                        label = "Add Expense",
                        color = Color(0xFFF44336),
                        onClick = {
                            isFabExpanded = false
                            navController.navigate("add_expense")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FabOption(
                        icon = Icons.Default.MoneyOff,
                        label = "Add Debt",
                        color = Color(0xFFFF9800),
                        onClick = {
                            isFabExpanded = false
                            navController.navigate("add_debt")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SummaryCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                UpcomingBillsSection()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(transactions, key = { it.id }) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onDelete = { transactions.remove(transaction) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FabOption(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = Color.White,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
fun UpcomingBillsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFF44336))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Due Date Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BillAlertItem(name = "Zuku Internet", amount = "Ksh 3,500", dueDate = "Due in 2 days")
            BillAlertItem(name = "KPLC Token", amount = "Ksh 1,000", dueDate = "Due tomorrow")
        }
    }
}

@Composable
fun BillAlertItem(name: String, amount: String, dueDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = dueDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(text = amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Total Balance", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Ksh 245,000.00",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(label = "Income", amount = "Ksh 300,000", color = Color(0xFF4CAF50))
                SummaryItem(label = "Expenses", amount = "Ksh 55,000", color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionListItem(transaction: Transaction, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(transaction.title) },
        supportingContent = { Text(transaction.date) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (transaction.amount >= 0) "Ksh ${transaction.amount}" else "-Ksh ${Math.abs(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    )
}

data class Transaction(val id: Int, val title: String, val amount: Double, val date: String)

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

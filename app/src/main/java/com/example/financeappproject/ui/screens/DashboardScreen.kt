package com.example.financeappproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Tracker") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add transaction logic */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SummaryCard()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            TransactionList()
        }
    }
}

@Composable
fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Total Balance", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$2,450.00",
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
                SummaryItem(label = "Income", amount = "$3,000", color = Color(0xFF4CAF50))
                SummaryItem(label = "Expenses", amount = "$550", color = Color(0xFFF44336))
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

data class Transaction(val id: Int, val title: String, val amount: String, val date: String)

@Composable
fun TransactionList() {
    val transactions = listOf(
        Transaction(1, "Grocery Store", "-$50.00", "Oct 24"),
        Transaction(2, "Salary", "+$3,000.00", "Oct 23"),
        Transaction(3, "Coffee Shop", "-$5.50", "Oct 22"),
        Transaction(4, "Rent", "-$500.00", "Oct 20"),
    )

    LazyColumn {
        items(transactions) { transaction ->
            ListItem(
                headlineContent = { Text(transaction.title) },
                supportingContent = { Text(transaction.date) },
                trailingContent = {
                    Text(
                        text = transaction.amount,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.amount.startsWith("+")) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            )
            HorizontalDivider()
        }
    }
}

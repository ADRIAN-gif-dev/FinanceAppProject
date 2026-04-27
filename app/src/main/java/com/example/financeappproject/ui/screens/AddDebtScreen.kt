package com.example.financeappproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(navController: NavController) {
    var debtName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    
    val pendingDebts = remember {
        mutableStateListOf(
            DebtItem("Bank Loan", 50000.0, "Nov 05", "Pending"),
            DebtItem("Friend - John", 2000.0, "Oct 30", "Overdue"),
            DebtItem("M-Shwari", 4500.0, "Nov 12", "Pending")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Debt") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = debtName,
                onValueChange = { debtName = it },
                label = { Text("Debt Name (e.g., Bank Loan)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Ksh)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (e.g., 2024-11-15)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Save logic */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Save Debt")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Pending Debts & Due Dates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(pendingDebts) { debt ->
                    ListItem(
                        headlineContent = { Text(debt.name) },
                        supportingContent = { 
                            Text("Due: ${debt.dueDate} • ${debt.status}") 
                        },
                        trailingContent = {
                            Text(
                                "Ksh ${debt.amount}",
                                color = if(debt.status == "Overdue") Color.Red else Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

data class DebtItem(val name: String, val amount: Double, val dueDate: String, val status: String)

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
fun AddIncomeScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    
    val previousIncomes = remember {
        mutableStateListOf(
            IncomeItem("Salary", 300000.0, "Oct 23"),
            IncomeItem("Freelance", 15000.0, "Oct 15"),
            IncomeItem("Dividends", 2500.0, "Oct 01")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Income") },
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
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Ksh)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source (e.g., Salary, Bonus)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Save logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Income")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Previous Incomes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(previousIncomes) { income ->
                    ListItem(
                        headlineContent = { Text(income.source) },
                        supportingContent = { Text(income.date) },
                        trailingContent = {
                            Text(
                                "Ksh ${income.amount}",
                                color = Color(0xFF4CAF50),
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

data class IncomeItem(val source: String, val amount: Double, val date: String)

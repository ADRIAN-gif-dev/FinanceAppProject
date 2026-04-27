package com.example.financeappproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financeappproject.ui.components.FinanceBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavController) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val categories = remember { mutableStateListOf<BudgetCategory>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { FinanceBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCategoryDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (categories.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No budget categories yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(categories) { category ->
                    BudgetCategoryCard(
                        category = category,
                        onAddItem = { itemName, itemAmount ->
                            category.items.add(BudgetItem(itemName, itemAmount))
                            category.spentAmount += itemAmount
                        },
                        onDeleteCategory = { categories.remove(category) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, limit ->
                categories.add(BudgetCategory(name, limit))
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun BudgetCategoryCard(
    category: BudgetCategory,
    onAddItem: (String, Double) -> Unit,
    onDeleteCategory: () -> Unit
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    val remaining = category.limit - category.spentAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDeleteCategory) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Budget Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (remaining >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Budget: Ksh ${category.limit}", style = MaterialTheme.typography.bodyMedium)
                    Text("Spent: Ksh ${category.spentAmount}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Remaining: Ksh $remaining",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining >= 0) Color.Unspecified else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            category.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.name)
                    Text("Ksh ${item.amount}", fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
            }

            TextButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item")
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, amount ->
                onAddItem(name, amount)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Budget Category") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Monthly Limit (Ksh)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, limit.toDoubleOrNull() ?: 0.0) }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Budget") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Ksh)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, amount.toDoubleOrNull() ?: 0.0) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

data class BudgetCategory(
    val name: String,
    val limit: Double,
    var spentAmount: Double = 0.0,
    val items: MutableList<BudgetItem> = mutableStateListOf()
)

data class BudgetItem(val name: String, val amount: Double)

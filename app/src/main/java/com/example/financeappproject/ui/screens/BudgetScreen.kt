package com.example.financeappproject.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financeappproject.RetrofitClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.Budget
import com.example.financeappproject.ui.components.FinanceBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val categories = remember { mutableStateListOf<Budget>() }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch budgets on load
    LaunchedEffect(Unit) {
        isLoading = true
        RetrofitClient.getSupabaseApi().getBudgets(
            SupabaseConfig.API_KEY,
            "Bearer ${SupabaseConfig.API_KEY}",
            "eq.$userId"
        ).enqueue(object : Callback<List<Budget>> {
            override fun onResponse(call: Call<List<Budget>>, response: Response<List<Budget>>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    categories.clear()
                    categories.addAll(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Budget>>, t: Throwable) {
                isLoading = false
                Log.e("API_ERROR", t.message ?: "Error fetching budgets")
            }
        })
    }

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
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading && categories.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budget categories yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    items(categories) { budget ->
                        BudgetCategoryCard(
                            budget = budget,
                            onDeleteCategory = { categories.remove(budget) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, limit ->
                val newBudget = Budget().apply {
                    this.budget_id = UUID.randomUUID().toString()
                    this.user_id = userId
                    this.category_name = name
                    this.monthly_limit = limit
                    this.current_spend = 0.0
                }

                RetrofitClient.getSupabaseApi().createBudget(
                    SupabaseConfig.API_KEY,
                    "Bearer ${SupabaseConfig.API_KEY}",
                    newBudget
                ).enqueue(object : Callback<Void?> {
                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        if (response.isSuccessful) {
                            categories.add(newBudget)
                            sharedPrefs.edit().putBoolean("has_real_data", true).apply()
                        } else {
                            Toast.makeText(context, "Failed to create budget", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void?>, t: Throwable) {
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                    }
                })
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun BudgetCategoryCard(
    budget: Budget,
    onDeleteCategory: () -> Unit
) {
    val remaining = budget.monthly_limit - budget.current_spend

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
                Text(text = budget.category_name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDeleteCategory) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (remaining >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Budget: Ksh ${budget.monthly_limit}", style = MaterialTheme.typography.bodyMedium)
                    Text("Spent: Ksh ${budget.current_spend}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Remaining: Ksh $remaining",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining >= 0) Color.Unspecified else Color.Red
                    )
                }
            }
        }
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

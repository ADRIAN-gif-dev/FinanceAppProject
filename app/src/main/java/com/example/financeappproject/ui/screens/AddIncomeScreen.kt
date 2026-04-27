package com.example.financeappproject.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financeappproject.RetrofitClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.Transactions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("user_id", "") ?: ""

    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val previousIncomes = remember { mutableStateListOf<IncomeItem>() }

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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source (e.g., Salary, Bonus)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isLoading = true
                    val transaction = Transactions().apply {
                        this.source_id = UUID.randomUUID().toString()
                        this.user_id = userId
                        this.amount = amount.toDoubleOrNull() ?: 0.0
                        this.timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                    }

                    RetrofitClient.getSupabaseApi().logTransaction(
                        SupabaseConfig.API_KEY,
                        "Bearer ${SupabaseConfig.API_KEY}",
                        transaction
                    ).enqueue(object : Callback<Void?> {
                        override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                            isLoading = false
                            if (response.isSuccessful) {
                                // Clear dummy data flag if this is the first transaction
                                sharedPrefs.edit().putBoolean("has_real_data", true).apply()
                                navController.navigateUp()
                            }
                        }

                        override fun onFailure(call: Call<Void?>, t: Throwable) {
                            isLoading = false
                            Log.e("API_ERROR", t.message ?: "Unknown error")
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && amount.isNotEmpty() && source.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Save Income")
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

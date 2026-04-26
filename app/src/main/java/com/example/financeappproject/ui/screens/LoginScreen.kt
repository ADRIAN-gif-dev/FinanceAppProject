package com.example.financeappproject.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.financeappproject.RetrofitClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Pochiwise Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null

                val api = RetrofitClient.getSupabaseApi()
                // We search the database for a matching email AND password
                api.loginUser(
                    SupabaseConfig.API_KEY,
                    "Bearer ${SupabaseConfig.API_KEY}",
                    "eq.$email",
                    "eq.$password"
                ).enqueue(object : Callback<List<User>> {
                    override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                        isLoading = false
                        val userList = response.body()

                        if (response.isSuccessful && !userList.isNullOrEmpty()) {
                            // Success: At least one matching user was found!
                            Log.d("LOGIN", "Welcome back, ${userList[0].name}")
                            onLoginSuccess()
                        } else {
                            // Failure: No user found or wrong password
                            errorMessage = "Invalid email or password"
                        }
                    }

                    override fun onFailure(call: Call<List<User>>, t: Throwable) {
                        isLoading = false
                        errorMessage = "Network error: ${t.message}"
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
            Text("Don't have an account? Register")
        }
    }
}
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
import java.util.UUID

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // State for UI fields
    var name by remember { mutableStateOf("") } // Added Name to match your Database
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // UI Feedback states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Create Pochiwise Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Name Field (Required by your Schema!)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                // 1. Prepare the User Model
                val newUser = User().apply {
                    this.user_id = UUID.randomUUID().toString()
                    this.name = name
                    this.email = email
                    this.password_hash = password // In production, we'd hash this!
                    this.biometric_token = "none"
                }

                // 2. Use your Supabase Bridge
                val api = RetrofitClient.getSupabaseApi()
                api.createUser(SupabaseConfig.API_KEY, "Bearer ${SupabaseConfig.API_KEY}", newUser)
                    .enqueue(object : Callback<Void?> {
                        override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                            isLoading = false
                            if (response.isSuccessful) {
                                Log.d("REGISTER_SUCCESS", "User saved to Supabase!")
                                onRegisterSuccess()
                            } else {
                                val errorMsg = "Error ${response.code()}: ${response.message()}"
                                Log.e("REGISTER_ERROR", errorMsg)
                                errorMessage = "Registration failed. Try again."
                            }
                        }

                        override fun onFailure(call: Call<Void?>, t: Throwable) {
                            isLoading = false
                            errorMessage = "Connection error: ${t.message}"
                            Log.e("REGISTER_FAILURE", t.message ?: "Unknown error")
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register")
            }
        }

        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
            Text("Already have an account? Login")
        }
    }
}
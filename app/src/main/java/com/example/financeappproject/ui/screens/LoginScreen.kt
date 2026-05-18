package com.example.financeappproject.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.financeappproject.RetrofitClient
import com.example.financeappproject.SupabaseConfig
import com.example.financeappproject.models.User
import com.example.financeappproject.ui.components.BiometricAuth
import com.example.financeappproject.ui.components.SecureStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val secureStorage = remember { SecureStorage(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var biometricAvailable by remember { mutableStateOf(false) }
    
    val hasCredentials = remember { secureStorage.hasStoredCredentials() }
    
    // Check biometric availability
    LaunchedEffect(Unit) {
        activity?.let {
            biometricAvailable = BiometricAuth(it).canAuthenticate()
        }
    }
    
    // Auto-login via Biometrics if enabled
    LaunchedEffect(biometricAvailable) {
        if (biometricAvailable && hasCredentials && secureStorage.isBiometricEnabled()) {
            activity?.let { act ->
                BiometricAuth(act).authenticate(
                    onSuccess = {
                        Log.d("BIOMETRIC", "Biometric success, restoring session")
                        // Restore session into shared prefs for other screens
                        sharedPrefs.edit().apply {
                            putString("user_id", secureStorage.getUserId())
                            putString("user_name", secureStorage.getUserName())
                            putString("user_email", secureStorage.getEmail())
                            apply()
                        }
                        onLoginSuccess()
                    },
                    onError = { error -> Log.d("BIOMETRIC", "Error: $error") },
                    onFailed = { Log.d("BIOMETRIC", "Authentication failed") }
                )
            }
        }
    }

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

                RetrofitClient.getSupabaseApi().loginUser(
                    SupabaseConfig.API_KEY,
                    "Bearer ${SupabaseConfig.API_KEY}",
                    "eq.$email",
                    "eq.$password"
                ).enqueue(object : Callback<List<User>> {
                    override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                        isLoading = false
                        val userList = response.body()

                        if (response.isSuccessful && !userList.isNullOrEmpty()) {
                            val user = userList[0]
                            
                            // 1. Save to session SharedPreferences for the app's current run
                            sharedPrefs.edit().apply {
                                putString("user_id", user.user_id)
                                putString("user_name", user.name)
                                putString("user_email", user.email)
                                apply()
                            }
                            
                            // 2. Store securely for future biometric logins
                            secureStorage.saveCredentials(
                                userId = user.user_id,
                                name = user.name,
                                email = user.email,
                                token = "session_token_${user.user_id}" 
                            )
                            
                            onLoginSuccess()
                        } else {
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
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Login")
        }

        TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
            Text("Don't have an account? Register")
        }
        
        if (biometricAvailable && hasCredentials && secureStorage.isBiometricEnabled()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    activity?.let { act ->
                        BiometricAuth(act).authenticate(
                            onSuccess = {
                                sharedPrefs.edit().apply {
                                    putString("user_id", secureStorage.getUserId())
                                    putString("user_name", secureStorage.getUserName())
                                    putString("user_email", secureStorage.getEmail())
                                    apply()
                                }
                                onLoginSuccess()
                            },
                            onError = { errorMessage = it },
                            onFailed = { errorMessage = "Biometric authentication failed" }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Biometric Login")
            }
        }
    }
}

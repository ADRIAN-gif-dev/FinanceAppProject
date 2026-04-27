package com.example.financeappproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.financeappproject.ui.components.BiometricAuth
import com.example.financeappproject.ui.components.SecureStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val secureStorage = remember { SecureStorage(context) }
    val biometricAuth = remember { activity?.let { BiometricAuth(it) } }
    
    var isBiometricEnabled by remember { mutableStateOf(secureStorage.isBiometricEnabled()) }
    val hasCredentials = secureStorage.hasStoredCredentials()
    
    var biometricStatusMessage by remember { 
        mutableStateOf(
            if (!hasCredentials) "Register/Login first to use biometrics" 
            else biometricAuth?.getBiometricStatusMessage() ?: "Biometrics not available"
        ) 
    }
    val isNotEnrolled = biometricAuth?.isNotEnrolled() ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Security", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Login", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    biometricStatusMessage, 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!hasCredentials || isNotEnrolled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (biometricAuth?.canAuthenticate() == true) {
                                        isBiometricEnabled = true
                                        secureStorage.setBiometricEnabled(true)
                                    } else if (isNotEnrolled) {
                                        biometricAuth?.promptEnrollment()
                                    }
                                } else {
                                    isBiometricEnabled = false
                                    secureStorage.setBiometricEnabled(false)
                                }
                            },
                            enabled = hasCredentials && (biometricAuth?.canAuthenticate() == true || isNotEnrolled)
                        )
                    }
                    
                    if (!hasCredentials) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You must be logged in with a password before enabling biometric login.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (isNotEnrolled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { biometricAuth?.promptEnrollment() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Setup Biometrics")
                        }
                    }
                }
            }
        }
    }
}

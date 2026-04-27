package com.example.financeappproject.ui.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper class for biometric authentication.
 * Provides methods to check biometric availability and authenticate users.
 */
class BiometricAuth(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)

    /**
     * Check if biometric authentication is available on this device.
     * @return true if strong biometrics are available, false otherwise
     */
    fun canAuthenticate(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricPrompt.AUTHENTICATORS_BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Get a human-readable message for why biometrics are not available.
     */
    fun getBiometricStatusMessage(): String {
        return when (biometricManager.canAuthenticate(BiometricPrompt.AUTHENTICATORS_BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometrics available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometrics unsupported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Unknown biometric status"
            else -> "Biometrics unavailable"
        }
    }

    /**
     * Authenticate the user using biometrics.
     * @param title The title displayed in the biometric prompt
     * @param subtitle The subtitle displayed in the biometric prompt
     * @param negativeButtonText Text for the negative button (fallback)
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when there's an authentication error
     * @param onFailed Callback when authentication fails (wrong fingerprint etc.)
     */
    fun authenticate(
        title: String = "Biometric Login",
        subtitle: String = "Confirm your identity to access your account",
        negativeButtonText: String = "Use Password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricPrompt.AUTHENTICATORS_BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
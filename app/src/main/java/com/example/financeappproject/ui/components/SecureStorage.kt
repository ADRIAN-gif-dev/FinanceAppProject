package com.example.financeappproject.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for authentication credentials using encrypted shared preferences.
 * Uses AES-256 encryption to protect sensitive data like auth tokens and user email.
 */
class SecureStorage(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Save user credentials securely.
     * @param email The user's email address
     * @param token The authentication token (JWT)
     */
    fun saveCredentials(email: String, token: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    /**
     * Retrieve the stored authentication token.
     * @return The auth token or null if not found
     */
    fun getToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    /**
     * Retrieve the stored user email.
     * @return The user email or null if not found
     */
    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    /**
     * Check if credentials are stored.
     * @return true if both email and token exist
     */
    fun hasStoredCredentials(): Boolean {
        return getEmail() != null && getToken() != null
    }

    /**
     * Enable or disable biometric login.
     * @param enabled Whether biometric login should be enabled
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    /**
     * Check if biometric login is enabled.
     * @return true if biometric login is enabled
     */
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    /**
     * Clear all stored credentials.
     * Use this on logout or when clearing session data.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Clear only authentication data (keep biometric preference).
     */
    fun clearAuthData() {
        val biometricEnabled = isBiometricEnabled()
        prefs.edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_AUTH_TOKEN)
            .apply()
        // Restore biometric preference
        setBiometricEnabled(biometricEnabled)
    }
}
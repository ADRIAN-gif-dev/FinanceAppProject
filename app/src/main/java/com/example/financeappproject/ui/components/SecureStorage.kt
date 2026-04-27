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

    fun saveCredentials(email: String, token: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun hasStoredCredentials(): Boolean {
        return getEmail() != null && getToken() != null
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun clearAuthData() {
        val biometricEnabled = isBiometricEnabled()
        prefs.edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_AUTH_TOKEN)
            .apply()
        setBiometricEnabled(biometricEnabled)
    }
}

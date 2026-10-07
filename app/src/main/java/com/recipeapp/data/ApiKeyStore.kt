package com.recipeapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Holds the user's own Gemini API key, encrypted at rest on the device, typed
 * in once via SettingsScreen. Nothing is baked into the APK or source control
 * -- this replaces the old BuildConfig/local.properties approach.
 */
object ApiKeyStore {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_GEMINI = "gemini_api_key"

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getKey(context: Context): String? =
        prefs(context).getString(KEY_GEMINI, null)?.takeIf { it.isNotBlank() }

    fun setKey(context: Context, key: String) {
        prefs(context).edit().putString(KEY_GEMINI, key.trim()).apply()
    }

    fun clearKey(context: Context) {
        prefs(context).edit().remove(KEY_GEMINI).apply()
    }

    fun hasKey(context: Context): Boolean = !getKey(context).isNullOrBlank()
}

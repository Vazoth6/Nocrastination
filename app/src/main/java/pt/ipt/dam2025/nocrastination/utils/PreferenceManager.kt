package pt.ipt.dam2025.nocrastination.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

class PreferenceManager(context: Context) {


    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    // Authentication
    fun saveAuthToken(token: String) {
        Log.d("PreferenceManager", "💾 Salvando token: ${token.take(20)}...")
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        Log.d("PreferenceManager", "🔍 Token recuperado: ${if (token != null) "EXISTE" else "NULO"}")
        return token
    }

    fun clearAuthToken() {
        prefs.edit { remove("auth_token") }
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return this.prefs.getString("refresh_token", null)
    }

    fun clearRefreshToken() {
        prefs.edit().remove("refresh_token").apply()
    }

    // User info
    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    // App settings
    fun saveApiUrl(url: String) {
        prefs.edit().putString("api_url", url).apply()
    }

    fun getApiUrl(): String {
        return prefs.getString("api_url", "http://10.0.2.2:1337/api/") ?: "http://10.0.2.2:1337/api/"
    }

    // Clear all data (logout)
    fun clearAll() {
        Log.d("PreferenceManager", "🗑️ Limpando todas as preferências")
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }
}
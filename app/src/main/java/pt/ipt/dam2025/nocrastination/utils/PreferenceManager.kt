package pt.ipt.dam2025.nocrastination.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    // Authentication
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return this.prefs.getString("auth_token", null)
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
        prefs.edit().clear().apply()
    }
}
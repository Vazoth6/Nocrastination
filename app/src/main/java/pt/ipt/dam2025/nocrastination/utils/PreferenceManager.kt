package pt.ipt.dam2025.nocrastination.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

// Gestor de preferências para armazenamento local
class PreferenceManager(context: Context) {

    // Instância de SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    // Autenticação, guardar token de acesso
    fun saveAuthToken(token: String) {
        Log.d("PreferenceManager", "A salvar token: ${token.take(20)}...")
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    // Autenticação, obter token de acesso
    fun getAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        Log.d("PreferenceManager", "Token recuperado: ${if (token != null) "EXISTE" else "NULO"}")
        return token
    }

    // Autenticação, limpar token de acesso
    fun clearAuthToken() {
        prefs.edit { remove("auth_token") }
    }

    // Autenticação, guardar token de atualização
    fun saveRefreshToken(token: String) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    // Autenticação, obter token de atualização
    fun getRefreshToken(): String? {
        return this.prefs.getString("refresh_token", null)
    }

    // Autenticação, limpar token de atualização
    fun clearRefreshToken() {
        prefs.edit().remove("refresh_token").apply()
    }

    // Informação do utilizador, guardar ID
    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    // Informação do utilizador, obter ID
    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    // Configurações da aplicação, guardar URL da API
    fun saveApiUrl(url: String) {
        prefs.edit().putString("api_url", url).apply()
    }

    // Configurações da aplicação, obter URL da API
    fun getApiUrl(): String {
        return prefs.getString("api_url", "http://10.0.2.2:1337/api/") ?: "http://10.0.2.2:1337/api/"
    }

    // Limpar todos os dados (para logout)
    fun clearAll() {
        Log.d("PreferenceManager", "A limpar todas as preferências...")
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }
}
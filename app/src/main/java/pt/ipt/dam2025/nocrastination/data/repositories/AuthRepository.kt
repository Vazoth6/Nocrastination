package pt.ipt.dam2025.nocrastination.data.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipt.dam2025.nocrastination.data.remote.ApiClient
import pt.ipt.dam2025.nocrastination.data.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import java.lang.Exception

class AuthRepository(private val context: Context) {

    private val authApi = ApiClient.getAuthApi(context)
    private val preferenceManager = PreferenceManager(context)

    suspend fun login(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Save tokens
                        preferenceManager.saveAuthToken(authResponse.jwt)
                        preferenceManager.saveUserId(authResponse.user.id)

                        // Update API client with new token
                        // (AuthInterceptor will automatically add it to requests)

                        Result.success(Unit)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("Login failed: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(RegisterRequest(username, email, password))

                if (response.isSuccessful) {
                    // Auto-login after registration
                    login(email, password)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("Registration failed: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        preferenceManager.clearAll()
    }

    fun isLoggedIn(): Boolean {
        return preferenceManager.getAuthToken() != null
    }
}
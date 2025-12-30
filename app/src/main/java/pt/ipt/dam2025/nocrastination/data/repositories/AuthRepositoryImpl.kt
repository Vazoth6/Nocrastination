// AuthRepositoryImpl.kt
package pt.ipt.dam2025.nocrastination.data.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import pt.ipt.dam2025.nocrastination.utils.Resource

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val preferenceManager: PreferenceManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Correção: Strapi espera "identifier" não "email"
                val response = authApi.login(LoginRequest(
                    identifier = email,  // Campo correto para Strapi
                    password = password
                ))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Verifique se o campo é "jwt" ou "token"
                        val token = authResponse.jwt // Ou authResponse.token
                        preferenceManager.saveAuthToken(token)
                        preferenceManager.saveUserId(authResponse.user.id)
                        Resource.Success(Unit)
                    } ?: Resource.Error("Empty response")
                } else {
                    // Melhor tratamento de erro
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Resource.Error("Login failed ($errorCode): $errorBody")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.message}")
            }
        }
    }


    override suspend fun register(username: String, email: String, password: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(RegisterRequest(username, email, password))

                if (response.isSuccessful) {
                    login(email, password)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Resource.Error("Registration failed: $errorBody")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    override fun logout() {
        preferenceManager.clearAll()
    }

    override fun isLoggedIn(): Boolean {
        return preferenceManager.getAuthToken() != null
    }
}
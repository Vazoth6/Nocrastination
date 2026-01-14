package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.toDomain
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import pt.ipt.dam2025.nocrastination.utils.Resource

class AuthRepositoryImpl(
    private val authApi: AuthApi, // API para chamadas de autenticação
    private val preferenceManager: PreferenceManager // Gestor de preferências para persistência local
) : AuthRepository {

    /**
     * Efetua o login do utilizador
     * @param email Email ou identificador do utilizador
     * @param password Palavra-passe do utilizador
     * @return Resource/Recruso com resultado da operação
     */
    override suspend fun login(email: String, password: String): Resource<Unit> {
        return withContext(Dispatchers.IO) { // Executa em background thread
            try {
                // Faz a chamada à API com os dados de login
                val response = authApi.login(LoginRequest(
                    identifier = email, // Campo pode ser email ou nome de utilizador
                    password = password
                ))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Guarda o token JWT e ID do utilizador localmente
                        val token = authResponse.jwt // Ou authResponse.token
                        preferenceManager.saveAuthToken(token)
                        preferenceManager.saveUserId(authResponse.user.id)
                        Resource.Success(Unit)
                    } ?: Resource.Error("Resposta vazia") // Resposta sem corpo
                } else {
                    // Tratamento detalhado de erros da API
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Resource.Error("Login falhou ($errorCode): $errorBody")
                }
            } catch (e: Exception) {
                // Erro de rede ou outras exceções
                Resource.Error("Erro de rede: ${e.message}")
            }
        }
    }

    /**
     * Regista um novo utilizador
     * @param username Nome de utilizador
     * @param email Email do utilizador
     * @param password Palavra-passe do utilizador
     * @return Resource com resultado da operação
     */
    override suspend fun register(username: String, email: String, password: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(RegisterRequest(username, email, password))

                if (response.isSuccessful) {
                    // Após registo bem-sucedido, faz login automaticamente
                    login(email, password)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Resource.Error("Registo falhou: $errorBody")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    /**
     * Efetua logout do utilizador
     * Limpa todos os dados de autenticação guardados localmente
     */
    override fun logout() {
        preferenceManager.clearAll() // Remove token, userId, etc.
    }

    /**
     * Verifica se o utilizador está autenticado
     * @return true se existir um token guardado, false caso contrário
     */
    override fun isLoggedIn(): Boolean {
        return preferenceManager.getAuthToken() != null
    }

    /**
     * Obtém o perfil do utilizador atualmente autenticado
     * @return Resource com UserProfile ou mensagem de erro
     */
    override suspend fun getCurrentUser(): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", " A efetuar busca do utilizador atual via /api/users/me")

                val response = authApi.getCurrentUser()

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        Log.d("AuthRepository", " Utilizador encontrado: ${userResponse.email}")

                        // Converte a resposta da API para o modelo de domínio
                        val userProfile = userResponse.toDomain()
                        return@withContext Resource.Success(userProfile)
                    } else {
                        Log.e("AuthRepository", " Resposta vazia")
                        return@withContext Resource.Error("Resposta vazia do servidor")
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Log.e("AuthRepository", " Erro $errorCode: $errorBody")

                    return@withContext Resource.Error("Falha ao obter utilizador: $errorCode")
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", " Exceção: ${e.message}", e)
                return@withContext Resource.Error("Erro de rede: ${e.message}")
            }
        }
    }
}
package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.AuthResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // Endpoint para autenticação/login de utilizador
    // Utiliza o metodo POST e recebe um objeto LoginRequest no corpo da requisição
    // Retorna um AuthResponse que deve conter o token JWT e dados do utilizador
    @POST("api/auth/local")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Endpoint para registo de novo utilizador
    // Similar ao login, mas para criação de conta com dados de registo
    @POST("api/auth/local/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Endpoint para obter os dados do utilizador atualmente autenticado
    // Requer que o token JWT seja enviado no header da requisição (normalmente via interceptor)
    // Útil para obter informações atualizadas do perfil do utilizador
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>
}
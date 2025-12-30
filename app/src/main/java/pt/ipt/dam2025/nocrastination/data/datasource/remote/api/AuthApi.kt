package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.AuthResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    @POST("api/auth/local")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/local/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>
}
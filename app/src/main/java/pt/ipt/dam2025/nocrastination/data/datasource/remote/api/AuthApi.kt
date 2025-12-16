package pt.ipt.dam2025.nocrastination.data.remote.api

import pt.ipt.dam2025.nocrastination.data.remote.models.requests.LoginRequest
import pt.ipt.dam2025.nocrastination.data.remote.models.requests.RegisterRequest
import pt.ipt.dam2025.nocrastination.data.remote.models.responses.ApiResponse
import pt.ipt.dam2025.nocrastination.data.remote.models.responses.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/local/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/local")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // Strapi doesn't have built-in logout endpoint
    // You'll need to handle JWT invalidation on client-side
}
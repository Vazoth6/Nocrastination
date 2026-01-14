package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.FocusLocationRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationResponse
import retrofit2.Response
import retrofit2.http.*

interface FocusLocationApi {
    // Obtém todas as localizações de foco
    @GET("api/focus-locations")
    suspend fun getFocusLocations(): Response<FocusLocationListResponse>

    // Obtém uma localização específica por ID
    @GET("api/focus-locations/{id}")
    suspend fun getFocusLocationById(@Path("id") id: Int): Response<FocusLocationResponse>

    // Cria uma nova localização de foco
    @POST("api/focus-locations")
    suspend fun createFocusLocation(@Body request: FocusLocationRequest): Response<FocusLocationResponse>

    // Atualiza uma localização existente (operador idempotente)
    @PUT("api/focus-locations/{id}")
    suspend fun updateFocusLocation(
        @Path("id") id: Int,
        @Body request: FocusLocationRequest
    ): Response<FocusLocationResponse>

    // Elimina uma localização - retorna Unit (sem corpo na resposta)
    @DELETE("api/focus-locations/{id}")
    suspend fun deleteFocusLocation(@Path("id") id: Int): Response<Unit>

    // Endpoint especial para alternar o estado de uma localização (ativo/inativo)
    @PUT("api/focus-locations/{id}/toggle")
    suspend fun toggleFocusLocation(
        @Path("id") id: Int,
        @Body request: Map<String, Boolean>
    ): Response<FocusLocationResponse>
}
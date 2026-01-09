package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.CreateFocusLocationRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdateFocusLocationRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationResponse
import retrofit2.Response
import retrofit2.http.*

interface FocusLocationApi {
    @GET("api/focus-locations")
    suspend fun getFocusLocations(): Response<FocusLocationListResponse>

    @GET("api/focus-locations/{id}")
    suspend fun getFocusLocationById(@Path("id") id: Int): Response<FocusLocationResponse>

    @POST("api/focus-locations")
    suspend fun createFocusLocation(@Body request: CreateFocusLocationRequest): Response<FocusLocationResponse>

    @PUT("api/focus-locations/{id}")
    suspend fun updateFocusLocation(
        @Path("id") id: Int,
        @Body request: UpdateFocusLocationRequest
    ): Response<FocusLocationResponse>

    @DELETE("api/focus-locations/{id}")
    suspend fun deleteFocusLocation(@Path("id") id: Int): Response<Unit>

    @PUT("api/focus-locations/{id}/toggle")
    suspend fun toggleFocusLocation(
        @Path("id") id: Int,
        @Body request: Map<String, Boolean>
    ): Response<FocusLocationResponse>
}
// data/datasource/remote/api/PomodoroApi.kt
package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.CreatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.PomodoroListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.PomodoroSessionResponse
import retrofit2.Response
import retrofit2.http.*

interface PomodoroApi {

    // Get all pomodoro sessions with optional filters
    @GET("api/pomodoro-sessions")
    suspend fun getPomodoroSessions(
        @Query("filters[startTime][\$gte]") startTimeGte: String? = null,
        @Query("filters[startTime][\$lte]") startTimeLte: String? = null,
        @Query("filters[task][id]") taskId: Int? = null,
        @Query("filters[completed][\$eq]") completed: Boolean? = null
    ): Response<PomodoroListResponse>

    // Get pomodoro session by ID
    @GET("api/pomodoro-sessions/{id}")
    suspend fun getPomodoroSessionById(
        @Path("id") id: Int
    ): Response<PomodoroSessionResponse>

    // Create a new pomodoro session
    @POST("api/pomodoro-sessions")
    suspend fun createSession(
        @Body request: CreatePomodoroSessionRequest
    ): Response<PomodoroSessionResponse>

    // Update an existing pomodoro session
    @PUT("api/pomodoro-sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Int,
        @Body request: UpdatePomodoroSessionRequest
    ): Response<PomodoroSessionResponse>

    // Delete a pomodoro session
    @DELETE("api/pomodoro-sessions/{id}")
    suspend fun deleteSession(
        @Path("id") id: Int
    ): Response<Unit>
}
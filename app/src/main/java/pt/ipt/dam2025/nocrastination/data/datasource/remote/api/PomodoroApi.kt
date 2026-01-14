package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.CreatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.PomodoroListResponse
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.PomodoroSessionResponse
import retrofit2.Response
import retrofit2.http.*

interface PomodoroApi {

    // Obtém sessões Pomodoro com filtros opcionais
    // Usa query parameters específicos com sintaxe de filtro avançado
    // [$gte] = maior ou igual
    // [$lte] = menor ou igual
    // [$eq] = igual a
    @GET("api/pomodoro-sessions")
    suspend fun getPomodoroSessions(
        @Query("filters[startTime][\$gte]") startTimeGte: String? = null,
        @Query("filters[startTime][\$lte]") startTimeLte: String? = null,
        @Query("filters[task][id]") taskId: Int? = null,
        @Query("filters[completed][\$eq]") completed: Boolean? = null
    ): Response<PomodoroListResponse>

    // Obtém uma sessão específica por ID
    @GET("api/pomodoro-sessions/{id}")
    suspend fun getPomodoroSessionById(
        @Path("id") id: Int
    ): Response<PomodoroSessionResponse>

    // Cria uma nova sessão Pomodoro
    @POST("api/pomodoro-sessions")
    suspend fun createSession(
        @Body request: CreatePomodoroSessionRequest
    ): Response<PomodoroSessionResponse>

    // Atualiza uma sessão existente
    @PUT("api/pomodoro-sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Int,
        @Body request: UpdatePomodoroSessionRequest
    ): Response<PomodoroSessionResponse>

    // Elimina uma sessão
    @DELETE("api/pomodoro-sessions/{id}")
    suspend fun deleteSession(
        @Path("id") id: Int
    ): Response<Unit>
}
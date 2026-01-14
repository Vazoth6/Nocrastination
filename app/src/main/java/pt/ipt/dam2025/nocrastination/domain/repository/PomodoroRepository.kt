package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import java.util.*

interface PomodoroRepository {

    /**
     * Inicia uma nova sessão Pomodoro
     */
    suspend fun startSession(session: PomodoroSession): Result<PomodoroSession>

    /**
     * Marca uma sessão Pomodoro como completada
     * @param endTime Data/hora de fim em formato ISO 8601
     */
    suspend fun completeSession(sessionId: Int, endTime: String): Result<PomodoroSession>

    /**
     * Obtém sessões Pomodoro por data específica
     */
    suspend fun getSessionsByDate(date: Date): Result<List<PomodoroSession>>

    /**
     * Obtém sessões Pomodoro associadas a uma tarefa
     */
    suspend fun getSessionsByTask(taskId: Int): Result<List<PomodoroSession>>

    /**
     * Obtém sessões Pomodoro do dia atual
     */
    suspend fun getTodaySessions(): Result<List<PomodoroSession>>

    /**
     * Obtém uma sessão Pomodoro específica pelo ID
     */
    suspend fun getSessionById(id: Int): Result<PomodoroSession>

    /**
     * Atualiza uma sessão Pomodoro existente
     */
    suspend fun updateSession(session: PomodoroSession): Result<PomodoroSession>

    /**
     * Elimina uma sessão Pomodoro
     */
    suspend fun deleteSession(sessionId: Int): Result<Unit>
}
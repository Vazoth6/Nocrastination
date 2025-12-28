// domain/repository/PomodoroRepository.kt
package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.Result  // Fixed import
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import java.time.LocalDate

interface PomodoroRepository {
    suspend fun startSession(session: PomodoroSession): Result<PomodoroSession>
    suspend fun completeSession(sessionId: Int, endTime: String): Result<PomodoroSession>
    suspend fun getSessionsByDate(date: LocalDate): Result<List<PomodoroSession>>
    suspend fun getSessionsByTask(taskId: Int): Result<List<PomodoroSession>>
    suspend fun getTodaySessions(): Result<List<PomodoroSession>>
    suspend fun getSessionById(id: Int): Result<PomodoroSession>
    suspend fun updateSession(session: PomodoroSession): Result<PomodoroSession>
    suspend fun deleteSession(sessionId: Int): Result<Unit>
}
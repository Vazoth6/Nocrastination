// data/repository/PomodoroRepositoryImpl.kt
package pt.ipt.dam2025.nocrastination.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.PomodoroApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.mapper.PomodoroMapper
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import pt.ipt.dam2025.nocrastination.domain.repository.PomodoroRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class PomodoroRepositoryImpl constructor(
    private val pomodoroApi: PomodoroApi,
    private val pomodoroMapper: PomodoroMapper
) : PomodoroRepository {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun startSession(session: PomodoroSession): Result<PomodoroSession> {
        return try {
            val request = pomodoroMapper.mapToCreateRequest(session)
            val response = pomodoroApi.createSession(request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val createdSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdSession)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to create session"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun completeSession(sessionId: Int, endTime: String): Result<PomodoroSession> {
        return try {
            // Don't parse the endTime, use it as is (it should already be in ISO format)
            val request = UpdatePomodoroSessionRequest(
                data = UpdatePomodoroSessionRequest.Data(
                    attributes = UpdatePomodoroSessionRequest.Attributes(
                        endTime = endTime,
                        completed = true
                    )
                )
            )

            val response = pomodoroApi.updateSession(sessionId, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val updatedSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedSession)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to complete session"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSessionsByDate(date: LocalDate): Result<List<PomodoroSession>> {
        return try {
            // Convert LocalDate to start and end of day strings
            val dateString = date.toString() // YYYY-MM-DD
            val startOfDay = "$dateString 00:00:00.000Z"
            val endOfDay = "$dateString 23:59:59.999Z"

            val response = pomodoroApi.getPomodoroSessions(
                startTimeGte = startOfDay,
                startTimeLte = endOfDay
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val sessions = apiResponse.data.map { pomodoroMapper.mapToDomain(it) }
                    Result.Success(sessions)
                } ?: Result.Success(emptyList())
            } else {
                Result.Error(Exception("Failed to fetch sessions"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSessionsByTask(taskId: Int): Result<List<PomodoroSession>> {
        return try {
            val response = pomodoroApi.getPomodoroSessions(taskId = taskId)

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val sessions = apiResponse.data.map { pomodoroMapper.mapToDomain(it) }
                    Result.Success(sessions)
                } ?: Result.Success(emptyList())
            } else {
                Result.Error(Exception("Failed to fetch sessions for task"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTodaySessions(): Result<List<PomodoroSession>> {
        return try {
            val today = LocalDate.now()
            getSessionsByDate(today)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSessionById(id: Int): Result<PomodoroSession> {
        return try {
            val response = pomodoroApi.getPomodoroSessionById(id)

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val session = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(session)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateSession(session: PomodoroSession): Result<PomodoroSession> {
        return try {
            val request = pomodoroMapper.mapToUpdateRequest(session)
            val response = pomodoroApi.updateSession(session.id, request)

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val updatedSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedSession)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to update session"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteSession(sessionId: Int): Result<Unit> {
        return try {
            val response = pomodoroApi.deleteSession(sessionId)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to delete session"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
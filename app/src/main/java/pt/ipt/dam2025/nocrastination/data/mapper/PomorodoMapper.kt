package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.*
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.PomodoroAttributes
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.StrapiData
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import pt.ipt.dam2025.nocrastination.domain.models.SessionType
import java.text.SimpleDateFormat
import java.util.*

class PomodoroMapper constructor() {

    // Formatter para datas ISO 8601 (formato utilizado pelo Strapi/APIs REST)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") // Usa UTC para consistência
    }

    // Converte resposta da API para modelo de domínio
    fun mapToDomain(data: StrapiData<PomodoroAttributes>): PomodoroSession {
        // "Parse timestamps" para "Long milliseconds"
        val startTimeMillis = parseToMillis(data.attributes.startTime)
        val endTimeMillis = data.attributes.endTime?.let { parseToMillis(it) }

        return PomodoroSession(
            id = data.id,
            sessionType = when (data.attributes.sessionType.uppercase()) {
                "WORK" -> SessionType.WORK
                "SHORT_BREAK" -> SessionType.SHORT_BREAK
                "LONG_BREAK" -> SessionType.LONG_BREAK
                else -> SessionType.WORK // Fallback padrão
            },
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            durationMinutes = data.attributes.durationMinutes,
            completed = data.attributes.completed,
            taskId = data.attributes.taskId
        )
    }

    // Mapeia para request de criação
    // EndTime só é definido no UPDATE, não no CREATE
    fun mapToCreateRequest(session: PomodoroSession): CreatePomodoroSessionRequest {
        val startTimeString = formatFromMillis(session.startTime)

        return CreatePomodoroSessionRequest(
            data = CreatePomodoroSessionRequest.Data(
                attributes = CreatePomodoroSessionRequest.Attributes(
                    sessionType = when (session.sessionType) {
                        SessionType.WORK -> "WORK"
                        SessionType.SHORT_BREAK -> "SHORT_BREAK"
                        SessionType.LONG_BREAK -> "LONG_BREAK"
                    },
                    startTime = startTimeString,
                    durationMinutes = session.durationMinutes,
                    completed = session.completed,
                    task = session.taskId?.let {
                        CreatePomodoroSessionRequest.TaskData(it) // Relação com Task
                    }
                )
            )
        )
    }

    // Mapeia para request de atualização
    // Inclui endTime (que só existe quando sessão termina)
    fun mapToUpdateRequest(session: PomodoroSession): UpdatePomodoroSessionRequest {
        val endTimeString = session.endTime?.let { formatFromMillis(it) }

        return UpdatePomodoroSessionRequest(
            data = UpdatePomodoroSessionRequest.Data(
                attributes = UpdatePomodoroSessionRequest.Attributes(
                    sessionType = when (session.sessionType) {
                        SessionType.WORK -> "WORK"
                        SessionType.SHORT_BREAK -> "SHORT_BREAK"
                        SessionType.LONG_BREAK -> "LONG_BREAK"
                    },
                    startTime = formatFromMillis(session.startTime),
                    endTime = endTimeString,
                    durationMinutes = session.durationMinutes,
                    completed = session.completed,
                    task = session.taskId?.let {
                        UpdatePomodoroSessionRequest.TaskData(id = it)
                    }
                )
            )
        )
    }

    // Metodo privado com fallback robusto para parsing de datas
    private fun parseToMillis(timestamp: String): Long {
        return try {
            isoFormat.parse(timestamp)?.time ?: 0
        } catch (e: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(timestamp)?.time ?: 0
            } catch (e2: Exception) {
                0 // Fallback final
            }
        }
    }

    private fun formatFromMillis(millis: Long): String {
        return isoFormat.format(Date(millis))
    }
}
package pt.ipt.dam2025.nocrastination.domain.models

data class PomodoroSession(
    val id: Int,
    val sessionType: SessionType, // enum: WORK, SHORT_BREAK, LONG_BREAK
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val durationMinutes: Int,
    val completed: Boolean,
    val taskId: Int? // If linked to a task
)

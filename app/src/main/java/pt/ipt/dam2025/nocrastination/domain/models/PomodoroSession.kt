package pt.ipt.dam2025.nocrastination.domain.models

data class PomodoroSession(
    val id: Int,
    val sessionType: SessionType,
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Int,
    val completed: Boolean,
    val taskId: Int? = null
)

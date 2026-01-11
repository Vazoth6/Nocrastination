package pt.ipt.dam2025.nocrastination.domain.models

data class UserProfile(
    val userId: Int,
    val fullName: String,
    val bio: String?,
    val avatarUrl: String?,
    val timezone: String,
    val dailyGoalMinutes: Int,
    val pomodoroWorkDuration: Int,
    val pomodoroShortBreak: Int,
    val pomodoroLongBreak: Int
)


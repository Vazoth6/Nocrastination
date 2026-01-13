package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserProfileResponse
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

object UserProfileMapper {
    fun toDomain(response: UserProfileResponse): UserProfile {
        return UserProfile(
            userId = response.user?.id ?: 0,
            fullName = response.fullName ?: response.user?.username ?: "",
            bio = response.bio,
            avatarUrl = response.avatar?.url,
            timezone = response.timezone ?: "Europe/Lisbon",
            dailyGoalMinutes = response.dailyGoalMinutes ?: 240,
            pomodoroWorkDuration = response.pomodoroWorkDuration ?: 25,
            pomodoroShortBreak = response.pomodoroShortBreak ?: 5,
            pomodoroLongBreak = response.pomodoroLongBreak ?: 15,
            userEmail = response.user?.email ?: ""
        )
    }
}
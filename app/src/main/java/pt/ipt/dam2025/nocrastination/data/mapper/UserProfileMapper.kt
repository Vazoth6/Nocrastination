package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserProfileResponse
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

// OBJECT (Singleton) porque não tem estado, pode ser compartilhado
object UserProfileMapper {

    // Converte a resposta da API para modelo de domínio
    fun toDomain(response: UserProfileResponse): UserProfile {
        return UserProfile(
            userId = response.user?.id ?: 0, // Fallback seguro
            fullName = response.fullName ?: response.user?.username ?: "", // Prioridade: fullName > username
            bio = response.bio,
            avatarUrl = response.avatar?.url,
            timezone = response.timezone ?: "Europe/Lisbon", // Padrão para Portugal
            dailyGoalMinutes = response.dailyGoalMinutes ?: 0,
            pomodoroWorkDuration = response.pomodoroWorkDuration ?: 0,
            pomodoroShortBreak = response.pomodoroShortBreak ?: 0,
            pomodoroLongBreak = response.pomodoroLongBreak ?: 0,
            userEmail = response.user?.email ?: ""
        )
    }
}
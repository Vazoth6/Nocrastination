package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

data class AuthResponse(
    val jwt: String,
    val user: UserResponse
)

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatar") val avatar: AvatarResponse?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("dailyGoalMinutes") val dailyGoalMinutes: Int?,
    @SerializedName("pomodoroWorkDuration") val pomodoroWorkDuration: Int?,
    @SerializedName("pomodoroShortBreak") val pomodoroShortBreak: Int?,
    @SerializedName("pomodoroLongBreak") val pomodoroLongBreak: Int?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    data class AvatarResponse(
        @SerializedName("url") val url: String?
    )
}

// Adicione esta função de extensão para UserResponse
fun UserResponse.toDomain(): UserProfile {
    return UserProfile(
        userId = id,
        fullName = fullName ?: username,  // Se fullName for null, usa username
        bio = bio,
        avatarUrl = avatar?.url,
        timezone = timezone ?: "Europe/Lisbon",
        dailyGoalMinutes = dailyGoalMinutes ?: 240,
        pomodoroWorkDuration = pomodoroWorkDuration ?: 25,
        pomodoroShortBreak = pomodoroShortBreak ?: 5,
        pomodoroLongBreak = pomodoroLongBreak ?: 15
    )
}

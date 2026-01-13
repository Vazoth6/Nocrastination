package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

data class UserProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatar") val avatar: AvatarResponse?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("dailyGoalMinutes") val dailyGoalMinutes: Int?,
    @SerializedName("pomodoroWorkDuration") val pomodoroWorkDuration: Int?,
    @SerializedName("pomodoroShortBreak") val pomodoroShortBreak: Int?,
    @SerializedName("pomodoroLongBreak") val pomodoroLongBreak: Int?,
    @SerializedName("user") val user: UserDataResponse?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    data class AvatarResponse(
        @SerializedName("id") val id: Int?,
        @SerializedName("url") val url: String?,
        @SerializedName("formats") val formats: Map<String, Format>?
    ) {
        data class Format(
            @SerializedName("url") val url: String?
        )
    }

    data class UserDataResponse(
        @SerializedName("id") val id: Int,
        @SerializedName("username") val username: String,
        @SerializedName("email") val email: String
    )
}

// Mapper function
fun UserProfileResponse.toDomain(): UserProfile {
    return UserProfile(
        userId = user?.id ?: 0,
        fullName = fullName ?: user?.username ?: "",
        bio = bio,
        avatarUrl = avatar?.url,
        timezone = timezone ?: "Europe/Lisbon",
        dailyGoalMinutes = dailyGoalMinutes ?: 240,
        pomodoroWorkDuration = pomodoroWorkDuration ?: 25,
        pomodoroShortBreak = pomodoroShortBreak ?: 5,
        pomodoroLongBreak = pomodoroLongBreak ?: 15,
        userEmail = user?.email ?: ""
    )
}


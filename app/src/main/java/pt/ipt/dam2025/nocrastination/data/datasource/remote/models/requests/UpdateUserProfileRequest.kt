package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("data") val data: ProfileData
) {
    data class ProfileData(
        @SerializedName("fullName") val fullName: String? = null,
        @SerializedName("bio") val bio: String? = null,
        @SerializedName("timezone") val timezone: String? = null,
        @SerializedName("dailyGoalMinutes") val dailyGoalMinutes: Int? = null,
        @SerializedName("pomodoroWorkDuration") val pomodoroWorkDuration: Int? = null,
        @SerializedName("pomodoroShortBreak") val pomodoroShortBreak: Int? = null,
        @SerializedName("pomodoroLongBreak") val pomodoroLongBreak: Int? = null
    )
}
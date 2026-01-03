package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName

data class PomodoroSessionResponse(
    val data: StrapiData<PomodoroAttributes>
)

data class PomodoroListResponse(
    val data: List<StrapiData<PomodoroAttributes>>,
    val meta: Meta? = null
)

data class PomodoroAttributes(
    @SerializedName("sessionType")
    val sessionType: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String?,

    @SerializedName("durationMinutes")
    val durationMinutes: Int,

    @SerializedName("completed")
    val completed: Boolean,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    // Se o Strapi retornar como nested object
    @SerializedName("task")
    val task: TaskRelationData? = null,

    // Ou se retornar direto o ID (depende do controller)
    @SerializedName("taskId")
    val taskId: Int? = null,

    @SerializedName("userId")
    val userId: Int? = null
)

// Para relação nested
data class TaskRelationData(
    val data: TaskIdData?
)

data class TaskIdData(
    val id: Int
)

data class Meta(
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val total: Int
)
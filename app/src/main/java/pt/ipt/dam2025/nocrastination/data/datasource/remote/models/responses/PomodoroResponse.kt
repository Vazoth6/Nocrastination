// data/datasource/remote/models/responses/PomodoroResponse.kt
package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

data class PomodoroSessionResponse(
    val data: StrapiData<PomodoroAttributes>
)

data class PomodoroListResponse(
    val data: List<StrapiData<PomodoroAttributes>>,
    val meta: Meta? = null
)

data class PomodoroAttributes(
    val sessionType: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Int,
    val completed: Boolean,
    val task: TaskRelationData? = null
)

// Simplified task relation structure
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
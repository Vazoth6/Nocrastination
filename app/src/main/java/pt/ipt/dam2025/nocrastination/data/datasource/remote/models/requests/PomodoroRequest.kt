// data/datasource/remote/models/requests/PomodoroRequest.kt
package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

// Remove the nested TaskIdData and simplify
data class CreatePomodoroSessionRequest(
    val data: Data
) {
    data class Data(
        val attributes: Attributes
    )

    data class Attributes(
        val sessionType: String,
        val startTime: String,
        val durationMinutes: Int,
        val completed: Boolean = false,
        val task: TaskData? = null
    )

    // Simplified - Strapi expects task: { data: { id: X } }
    data class TaskData(
        val id: Int? = null
    )
}

data class UpdatePomodoroSessionRequest(
    val data: Data
) {
    data class Data(
        val attributes: Attributes
    )

    data class Attributes(
        val sessionType: String? = null,
        val startTime: String? = null,
        val endTime: String? = null,
        val durationMinutes: Int? = null,
        val completed: Boolean? = null,
        val task: TaskData? = null
    )

    // For updating task relation
    data class TaskData(
        val id: Int? = null,
        val connect: List<Int>? = null,
        val disconnect: List<Int>? = null
    )
}

// Remove CompleteSessionRequest - we'll use UpdatePomodoroSessionRequest instead
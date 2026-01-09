package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

data class CreateTaskRequest(
    val data: Data
) {
    data class Data(
        val attributes: Attributes
    )

    data class Attributes(
        val title: String,
        val description: String? = null,
        val dueDate: String? = null,
        val priority: String,
        val estimatedMinutes: Int? = null,
        val completed: Boolean = false
    )
}

data class UpdateTaskRequest(
    val data: Data
) {
    data class Data(
        val attributes: Attributes
    )

    data class Attributes(
        val title: String? = null,
        val description: String? = null,
        val dueDate: String? = null,
        val priority: String? = null,
        val estimatedMinutes: Int? = null,
        val completed: Boolean? = null,
        val completedAt: String? = null // Set this when marking as completed
    )
}

data class CompleteTaskRequest(
    val data: Data
) {
    data class Data(
        val attributes: Attributes
    )

    data class Attributes(
        val completed: Boolean = true,
        val completedAt: String // ISO timestamp
    )
}
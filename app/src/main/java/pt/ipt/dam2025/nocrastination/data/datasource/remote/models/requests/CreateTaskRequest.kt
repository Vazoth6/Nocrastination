package pt.ipt.dam2025.nocrastination.data.remote.models.requests

import com.google.gson.annotations.SerializedName

data class CreateTaskRequest(
    @SerializedName("data")
    val data: TaskData
)

data class TaskData(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("dueDate")
    val dueDate: String? = null,

    @SerializedName("priority")
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW

    @SerializedName("status")
    val status: String = "TODO", // TODO, IN_PROGRESS, COMPLETED, CANCELLED

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("estimatedMinutes")
    val estimatedMinutes: Int = 0
)
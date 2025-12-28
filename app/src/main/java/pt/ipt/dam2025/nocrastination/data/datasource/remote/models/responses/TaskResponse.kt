package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("data")
    val data: TaskData
)

data class TaskListResponse(
    @SerializedName("data")
    val data: List<TaskData>
)

data class TaskData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("attributes")
    val attributes: TaskAttributes
)

data class TaskAttributes(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("dueDate")
    val dueDate: String?,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("completed")
    val completed: Boolean,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
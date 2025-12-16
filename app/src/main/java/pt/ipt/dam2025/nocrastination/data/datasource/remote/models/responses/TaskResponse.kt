package pt.ipt.dam2025.nocrastination.data.remote.models.responses

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("attributes")
    val attributes: TaskAttributes,

    @SerializedName("meta")
    val meta: Map<String, Any>? = null
) {
    // Helper properties for easier access
    val title: String get() = attributes.title
    val description: String? get() = attributes.description
    val dueDate: String? get() = attributes.dueDate
    val priority: String get() = attributes.priority
    val status: String get() = attributes.status
    val category: String? get() = attributes.category
}

data class TaskAttributes(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("dueDate")
    val dueDate: String? = null,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("estimatedMinutes")
    val estimatedMinutes: Int = 0,

    @SerializedName("actualMinutes")
    val actualMinutes: Int = 0,

    @SerializedName("completedAt")
    val completedAt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("user")
    val user: UserData? = null
)

data class UserData(
    @SerializedName("data")
    val data: UserInfo? = null
)

data class UserInfo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("attributes")
    val attributes: Map<String, Any>? = null
)
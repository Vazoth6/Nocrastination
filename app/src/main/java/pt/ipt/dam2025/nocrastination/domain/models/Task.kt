// domain/models/Task.kt
package pt.ipt.dam2025.nocrastination.domain.models

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: String?, // ISO date string
    val priority: TaskPriority,
    val completed: Boolean = false,
    val completedAt: String?, // ISO date string
    val createdAt: String, // ISO date string
    val updatedAt: String // ISO date string
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}
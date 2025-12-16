package pt.ipt.dam2025.nocrastination.domain.models

import org.chromium.base.task.TaskPriority
import java.time.LocalDateTime

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: LocalDateTime?,
    val priority: TaskPriority, // enum
    val category: String?,
    val estimatedMinutes: Int?,
    val completed: Boolean,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)
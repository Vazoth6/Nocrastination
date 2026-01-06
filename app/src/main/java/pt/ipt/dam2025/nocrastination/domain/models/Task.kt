// domain/models/Task.kt
package pt.ipt.dam2025.nocrastination.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: String?, // ISO date string
    val priority: TaskPriority,
    val completed: Boolean = false,
    val completedAt: String?, // ISO date string
    val createdAt: String, // ISO date string
    val updatedAt: String, // ISO date string
    val estimatedMinutes: Int? = null
): Parcelable

@Parcelize
enum class TaskPriority: Parcelable  {
    LOW, MEDIUM, HIGH
}
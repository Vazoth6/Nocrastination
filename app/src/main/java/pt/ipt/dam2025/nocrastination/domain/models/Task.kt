package pt.ipt.dam2025.nocrastination.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Modelo de domínio que representa uma tarefa no sistema de produtividade.
 *
 * Esta classe define uma unidade de trabalho que o utilizador precisa completar,
 * com várias propriedades para gestão e priorização.
 */
@Parcelize
data class Task(
    // O ID é gerado pelo servidor Strapi
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

// Enumeração que representa os níveis de prioridade de uma tarefa.
@Parcelize
enum class TaskPriority: Parcelable  {
    LOW, MEDIUM, HIGH
}
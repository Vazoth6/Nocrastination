package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.Task

interface TaskRepository {
    suspend fun getTasks(): Result<List<Task>>
    suspend fun getTaskById(id: Int): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>
    suspend fun createTask(task: Task): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>
    suspend fun updateTask(task: Task): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>
    suspend fun completeTask(taskId: Int, completedAt: String): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>
    suspend fun deleteTask(taskId: Int): pt.ipt.dam2025.nocrastination.domain.models.Result<Unit>
}
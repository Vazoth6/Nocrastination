package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.Task

interface TaskRepository {
    /**
     * Obtém todas as tarefas do utilizador
     */
    suspend fun getTasks(): Result<List<Task>>

    /**
     * Obtém uma tarefa específica pelo ID
     */
    suspend fun getTaskById(id: Int): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>

    /**
     * Cria uma nova tarefa
     */
    suspend fun createTask(task: Task): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>

    /**
     * Atualiza uma tarefa existente
     */
    suspend fun updateTask(task: Task): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>

    /**
     * Marca uma tarefa como completada
     * @param completedAt Data/hora de conclusão em formato ISO 8601
     */
    suspend fun completeTask(taskId: Int, completedAt: String): pt.ipt.dam2025.nocrastination.domain.models.Result<Task>

    /**
     * Elimina uma tarefa
     */
    suspend fun deleteTask(taskId: Int): pt.ipt.dam2025.nocrastination.domain.models.Result<Unit>
}
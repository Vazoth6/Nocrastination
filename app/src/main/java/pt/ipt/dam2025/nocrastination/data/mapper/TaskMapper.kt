package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.*
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.TaskData
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.TaskPriority

class TaskMapper constructor() {

    // Converte a resposta da API para modelo de domínio
    fun mapToDomain(data: TaskData): Task {
        return Task(
            id = data.id,
            title = data.attributes.title,
            description = data.attributes.description ?: "", // Fallback para string vazia
            dueDate = data.attributes.dueDate,
            priority = when (data.attributes.priority.uppercase()) {
                "HIGH" -> TaskPriority.HIGH
                "MEDIUM" -> TaskPriority.MEDIUM
                "LOW" -> TaskPriority.LOW
                else -> TaskPriority.MEDIUM // Fallback padrão
            },
            completed = data.attributes.completed,
            completedAt = data.attributes.completedAt,
            createdAt = data.attributes.createdAt,
            updatedAt = data.attributes.updatedAt,
            estimatedMinutes = data.attributes.estimatedMinutes
        )
    }

    // Mapeia para criação, campos mínimos necessários
    fun mapToCreateRequest(task: Task): CreateTaskRequest {
        return CreateTaskRequest(
            data = CreateTaskRequest.Data(
                attributes = CreateTaskRequest.Attributes(
                    title = task.title,
                    description = task.description.takeIf { it.isNotBlank() },
                    dueDate = task.dueDate,
                    priority = task.priority.name,
                    completed = task.completed,
                    estimatedMinutes = task.estimatedMinutes
                )
            )
        )
    }

    // Mapeia para atualização, campos adicionais
    fun mapToUpdateRequest(task: Task): UpdateTaskRequest {
        return UpdateTaskRequest(
            data = UpdateTaskRequest.Data(
                attributes = UpdateTaskRequest.Attributes(
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    priority = task.priority.name,
                    completed = task.completed,
                    completedAt = task.completedAt,
                    estimatedMinutes = task.estimatedMinutes
                )
            )
        )
    }

    // Mapeamento especializado apenas para completar a tarefa
    fun mapToCompleteRequest(completedAt: String): CompleteTaskRequest {
        return CompleteTaskRequest(
            data = CompleteTaskRequest.Data(
                attributes = CompleteTaskRequest.Attributes(
                    completedAt = completedAt
                )
            )
        )
    }
}
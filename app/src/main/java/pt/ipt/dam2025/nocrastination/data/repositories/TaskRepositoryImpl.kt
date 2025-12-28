package pt.ipt.dam2025.nocrastination.data.repositories

import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.mapper.TaskMapper
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.domain.models.Result
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi,
    private val taskMapper: TaskMapper
) : TaskRepository {

    override suspend fun getTasks(): Result<List<Task>> {
        return try {
            val response = taskApi.getTasks()
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val tasks = apiResponse.data.map { taskMapper.mapToDomain(it) }
                    Result.Success(tasks)
                } ?: Result.Success(emptyList())
            } else {
                Result.Error(Exception("Failed to fetch tasks: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTaskById(id: Int): Result<Task> {
        return try {
            val response = taskApi.getTaskById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val task = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(task)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Task not found: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createTask(task: Task): Result<Task> {
        return try {
            val request = taskMapper.mapToCreateRequest(task)
            val response = taskApi.createTask(request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val createdTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdTask)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to create task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Task> {
        return try {
            val request = taskMapper.mapToUpdateRequest(task)
            val response = taskApi.updateTask(task.id, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val updatedTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedTask)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to update task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun completeTask(taskId: Int, completedAt: String): Result<Task> {
        return try {
            val request = taskMapper.mapToCompleteRequest(completedAt)
            val response = taskApi.completeTask(taskId, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val completedTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(completedTask)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to complete task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteTask(taskId: Int): Result<Unit> {
        return try {
            val response = taskApi.deleteTask(taskId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to delete task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
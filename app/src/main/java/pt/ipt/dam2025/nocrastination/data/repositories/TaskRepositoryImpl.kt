package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import com.google.gson.Gson
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.mapper.TaskMapper
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.domain.models.Result

class TaskRepositoryImpl constructor(
    private val taskApi: TaskApi,
    private val taskMapper: TaskMapper
) : TaskRepository {

    override suspend fun getTasks(): Result<List<Task>> {
        return try {
            Log.d("TaskRepository", "🔄 Buscando tarefas da API...")
            val response = taskApi.getTasks()

            Log.d("TaskRepository", "📡 GET Tasks - Código: ${response.code()}")
            Log.d("TaskRepository", "📡 GET Tasks - Mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("TaskRepository", "✅ ${apiResponse.data.size} tarefas recebidas")
                    val tasks = apiResponse.data.map { taskMapper.mapToDomain(it) }
                    Log.d("TaskRepository", "📋 Primeira tarefa (se existir): ${tasks.firstOrNull()?.title}")
                    Result.Success(tasks)
                } ?: run {
                    Log.w("TaskRepository", "⚠️ Resposta vazia (body é null)")
                    Result.Success(emptyList())
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("TaskRepository", "❌ Erro na resposta GET: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao buscar tarefas: ${response.code()} $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "❌ Exceção em getTasks: ${e.message}", e)
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
            Log.d("TaskRepository", "🔄 Criando tarefa: ${task.title}")
            Log.d("TaskRepository", "📋 Dados da tarefa: $task")

            val request = taskMapper.mapToCreateRequest(task)
            Log.d("TaskRepository", "📤 Request JSON: ${Gson().toJson(request)}")

            val response = taskApi.createTask(request)

            Log.d("TaskRepository", "📡 POST CreateTask - Código: ${response.code()}")
            Log.d("TaskRepository", "📡 POST CreateTask - Mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("TaskRepository", "✅ Tarefa criada com sucesso! ID: ${apiResponse.data.id}")
                    Log.d("TaskRepository", "📋 Tarefa criada: ${apiResponse.data.attributes.title}")
                    val createdTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdTask)
                } ?: run {
                    Log.e("TaskRepository", "⚠️ Resposta vazia na criação (body é null)")
                    Result.Error(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("TaskRepository", "❌ Erro na criação: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao criar tarefa: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "❌ Exceção ao criar tarefa: ${e.message}", e)
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
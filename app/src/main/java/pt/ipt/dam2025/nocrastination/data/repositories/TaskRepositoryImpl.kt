package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import com.google.gson.Gson
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.mapper.TaskMapper
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.domain.models.Result

class TaskRepositoryImpl constructor(
    private val taskApi: TaskApi, // API para operações de tarefas
    private val taskMapper: TaskMapper // Mapper para conversão entre DTOs e modelos de domínio
) : TaskRepository {

    /**
     * Obtém todas as tarefas do utilizador
     * @return Result com lista de Task ou erro
     */
    override suspend fun getTasks(): Result<List<Task>> {
        return try {
            Log.d("TaskRepository", " A efetuar busca das tarefas da API...")
            val response = taskApi.getTasks()

            Log.d("TaskRepository", " GET Tasks - Código: ${response.code()}")
            Log.d("TaskRepository", " GET Tasks - Mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("TaskRepository", " ${apiResponse.data.size} tarefas recebidas")
                    val tasks = apiResponse.data.map { taskMapper.mapToDomain(it) }
                    Log.d("TaskRepository", " Primeira tarefa (se existir): ${tasks.firstOrNull()?.title}")
                    Result.Success(tasks)
                } ?: run {
                    // Caso a resposta seja bem-sucedida mas sem corpo, retorna lista vazia
                    Log.w("TaskRepository", " Resposta vazia (body é null)")
                    Result.Success(emptyList())
                }
            } else {
                // Tratamento de erro HTTP
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("TaskRepository", " Erro na resposta GET: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao buscar tarefas: ${response.code()} $errorBody"))
            }
        } catch (e: Exception) {
            // Tratamento de exceções gerais (rede, parsing, etc.)
            Log.e("TaskRepository", " Exceção em getTasks: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém uma tarefa específica pelo ID
     * @param id ID da tarefa
     * @return Result com Task ou erro
     */
    override suspend fun getTaskById(id: Int): Result<Task> {
        return try {
            val response = taskApi.getTaskById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val task = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(task)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                Result.Error(Exception("Tarefa náo encontrada: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Cria uma nova tarefa
     * @param task Objeto Task com dados para criação
     * @return Result com a tarefa criada ou erro
     */
    override suspend fun createTask(task: Task): Result<Task> {
        return try {
            Log.d("TaskRepository", " Criando tarefa: ${task.title}")
            Log.d("TaskRepository", " Dados da tarefa: $task")

            // Converte o modelo de domínio para DTO de criação
            val request = taskMapper.mapToCreateRequest(task)
            // Log do request em formato JSON para debugging
            Log.d("TaskRepository", " Request JSON: ${Gson().toJson(request)}")

            val response = taskApi.createTask(request)

            Log.d("TaskRepository", " POST CreateTask - Código: ${response.code()}")
            Log.d("TaskRepository", " POST CreateTask - Mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("TaskRepository", " Tarefa criada com sucesso! ID: ${apiResponse.data.id}")
                    Log.d("TaskRepository", " Tarefa criada: ${apiResponse.data.attributes.title}")
                    val createdTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdTask)
                } ?: run {
                    Log.e("TaskRepository", " Resposta vazia na criação (body é null)")
                    Result.Error(Exception("Resposta vazia do servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("TaskRepository", " Erro na criação: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao criar tarefa: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", " Exceção ao criar tarefa: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Atualiza uma tarefa existente
     * @param task Objeto Task com dados atualizados
     * @return Result com a tarefa atualizada ou erro
     */
    override suspend fun updateTask(task: Task): Result<Task> {
        return try {
            val request = taskMapper.mapToUpdateRequest(task)
            val response = taskApi.updateTask(task.id, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val updatedTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedTask)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                Result.Error(Exception("Falha ao atualizar task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Marca uma tarefa como completada
     * @param taskId ID da tarefa a completar
     * @param completedAt Data/hora de conclusão em formato string
     * @return Result com a tarefa atualizada ou erro
     */
    override suspend fun completeTask(taskId: Int, completedAt: String): Result<Task> {
        return try {
            val request = taskMapper.mapToCompleteRequest(completedAt)
            val response = taskApi.completeTask(taskId, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val completedTask = taskMapper.mapToDomain(apiResponse.data)
                    Result.Success(completedTask)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                Result.Error(Exception("Falha ao completar task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Elimina uma tarefa
     * @param taskId ID da tarefa a eliminar
     * @return Result com Unit em caso de sucesso ou erro
     */
    override suspend fun deleteTask(taskId: Int): Result<Unit> {
        return try {
            val response = taskApi.deleteTask(taskId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Falha ao apagar task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
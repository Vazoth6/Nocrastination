package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.PomodoroApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdatePomodoroSessionRequest
import pt.ipt.dam2025.nocrastination.data.mapper.PomodoroMapper
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import pt.ipt.dam2025.nocrastination.domain.repository.PomodoroRepository
import java.text.SimpleDateFormat
import java.util.*

class PomodoroRepositoryImpl constructor(
    private val pomodoroApi: PomodoroApi, // API para operações de sessões Pomodoro
    private val pomodoroMapper: PomodoroMapper // Mapper para conversão entre DTOs e modelos
) : PomodoroRepository {

    // Formato ISO 8601 para datas/horas (padrão usado pela API)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") // usar UTC para consistência
    }

    // Formato apenas para datas (YYYY-MM-DD)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Inicia uma nova sessão Pomodoro
     * @param session Objeto PomodoroSession com dados iniciais
     * @return Result com a sessão criada ou erro
     */
    override suspend fun startSession(session: PomodoroSession): Result<PomodoroSession> {
        return try {
            Log.d("PomodoroRepository", " Iniciando sessão...")
            val request = pomodoroMapper.mapToCreateRequest(session)
            val response = pomodoroApi.createSession(request)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " Sessão criada: ${apiResponse.data.id}")
                    val createdSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdSession)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao criar sessão: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Marca uma sessão Pomodoro como completada
     * @param sessionId ID da sessão a completar
     * @param endTime Data/hora de fim em formato ISO 8601
     * @return Result com a sessão atualizada ou erro
     */
    override suspend fun completeSession(sessionId: Int, endTime: String): Result<PomodoroSession> {
        return try {
            Log.d("PomodoroRepository", " Completando sessão $sessionId...")

            val request = UpdatePomodoroSessionRequest(
                data = UpdatePomodoroSessionRequest.Data(
                    attributes = UpdatePomodoroSessionRequest.Attributes(
                        endTime = endTime,
                        completed = true
                    )
                )
            )

            val response = pomodoroApi.updateSession(sessionId, request)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " Sessão completada")
                    val updatedSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedSession)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao completar sessão: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém sessões Pomodoro por data específica
     * @param date Data para filtrar as sessões
     * @return Result com lista de sessões ou erro
     */
    override suspend fun getSessionsByDate(date: Date): Result<List<PomodoroSession>> {
        return try {
            Log.d("PomodoroRepository", " Buscando sessões por data...")

            // Converter Date para string no formato YYYY-MM-DD
            val dateString = dateOnlyFormat.format(date)
            val startOfDay = "${dateString}T00:00:00.000Z"
            val endOfDay = "${dateString}T23:59:59.999Z"

            val response = pomodoroApi.getPomodoroSessions(
                startTimeGte = startOfDay,
                startTimeLte = endOfDay
            )

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " ${apiResponse.data.size} sessões encontradas")
                    val sessions = apiResponse.data.map { pomodoroMapper.mapToDomain(it) }
                    Result.Success(sessions)
                } ?: Result.Success(emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao buscar sessões: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém sessões Pomodoro associadas a uma tarefa específica
     * @param taskId ID da tarefa
     * @return Result com lista de sessões ou erro
     */
    override suspend fun getSessionsByTask(taskId: Int): Result<List<PomodoroSession>> {
        return try {
            Log.d("PomodoroRepository", " A efetuar busca de sessões por tarefa $taskId...")

            val response = pomodoroApi.getPomodoroSessions(taskId = taskId)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " ${apiResponse.data.size} sessões encontradas")
                    val sessions = apiResponse.data.map { pomodoroMapper.mapToDomain(it) }
                    Result.Success(sessions)
                } ?: Result.Success(emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", "❌ Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao buscar sessões para tarefa: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém sessões Pomodoro do dia atual
     * @return Result com lista de sessões de hoje ou erro
     */
    override suspend fun getTodaySessions(): Result<List<PomodoroSession>> {
        return try {
            Log.d("PomodoroRepository", " A efetuar busca sessões de hoje...")
            val today = Date()
            // Reutiliza a lógica de getSessionsByDate
            getSessionsByDate(today)
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém uma sessão Pomodoro específica pelo ID
     * @param id ID da sessão
     * @return Result com a sessão ou erro
     */
    override suspend fun getSessionById(id: Int): Result<PomodoroSession> {
        return try {
            Log.d("PomodoroRepository", " A efetuar busca sessão por ID $id...")

            val response = pomodoroApi.getPomodoroSessionById(id)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " Sessão encontrada")
                    val session = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(session)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Sessão não encontrada: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Atualiza uma sessão Pomodoro existente
     * @param session Objeto PomodoroSession com dados atualizados
     * @return Result com a sessão atualizada ou erro
     */
    override suspend fun updateSession(session: PomodoroSession): Result<PomodoroSession> {
        return try {
            Log.d("PomodoroRepository", " A atualizar sessão ${session.id}...")

            val request = pomodoroMapper.mapToUpdateRequest(session)
            val response = pomodoroApi.updateSession(session.id, request)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("PomodoroRepository", " Sessão atualizada")
                    val updatedSession = pomodoroMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedSession)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao atualizar sessão: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Elimina uma sessão Pomodoro
     * @param sessionId ID da sessão a eliminar
     * @return Result com Unit em caso de sucesso ou erro
     */
    override suspend fun deleteSession(sessionId: Int): Result<Unit> {
        return try {
            Log.d("PomodoroRepository", " A apagar sessão $sessionId...")

            val response = pomodoroApi.deleteSession(sessionId)

            Log.d("PomodoroRepository", " Código: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("PomodoroRepository", " Sessão deletada")
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("PomodoroRepository", " Erro: ${response.code()} - $errorBody")
                Result.Error(Exception("Falha ao apagar sessão: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PomodoroRepository", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }
}
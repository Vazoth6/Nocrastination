package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.UIEvent
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Eventos UI
    private val _uiEvents = MutableSharedFlow<UIEvent>()
    val uiEvents = _uiEvents

    fun loadTasks() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = taskRepository.getTasks()) {
                is Result.Success -> {
                    _tasks.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao carregar tarefas"
                }
            }
            _loading.value = false
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = taskRepository.createTask(task)) {
                is Result.Success -> {
                    // Adiciona a nova tarefa e recarrega a lista do servidor
                    _tasks.value = _tasks.value + result.data
                    // Recarrega todas as tarefas para garantir sincronização
                    loadTasks()
                    // Enviar evento de sucesso
                    _uiEvents.emit(UIEvent.ShowToast("Tarefa criada com sucesso!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao criar tarefa"
                }
            }
            _loading.value = false
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = taskRepository.updateTask(task)) {
                is Result.Success -> {
                    _tasks.value = _tasks.value.map {
                        if (it.id == result.data.id) result.data else it
                    }
                    _uiEvents.emit(UIEvent.ShowToast("Tarefa atualizada!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao atualizar tarefa"
                }
            }
            _loading.value = false
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val completedAt = getCurrentISOTimestamp()
            when (val result = taskRepository.completeTask(taskId, completedAt)) {
                is Result.Success -> {
                    // Atualizar a lista local com a tarefa completa
                    _tasks.value = _tasks.value.map {
                        if (it.id == taskId) result.data else it
                    }
                    // Enviar evento de sucesso
                    _uiEvents.emit(UIEvent.ShowToast("✅ Tarefa concluída!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao concluir tarefa"
                }
            }
            _loading.value = false
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = taskRepository.deleteTask(taskId)) {
                is Result.Success -> {
                    _tasks.value = _tasks.value.filter { it.id != taskId }
                    _uiEvents.emit(UIEvent.ShowToast("Tarefa eliminada!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao eliminar tarefa"
                }
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Filtra tarefas concluídas há mais de 24 horas localmente
     */
    private fun filterOldCompletedTasks(tasks: List<Task>): List<Task> {
        return tasks.filter { task ->
            // Manter tarefas não concluídas
            if (!task.completed) return@filter true

            // Se estiver concluída, verificar se foi há mais de 24 horas
            val completedAt = task.completedAt ?: return@filter true
            val isOlderThan24Hours = isDateOlderThan24Hours(completedAt)

            // Manter apenas se NÃO for mais antiga que 24 horas
            !isOlderThan24Hours
        }
    }

    /**
     * Limpa tarefas concluídas há mais de 24 horas do servidor
     */
    private fun cleanupOldCompletedTasks(tasks: List<Task>) {
        viewModelScope.launch {
            tasks.forEach { task ->
                if (task.completed) {
                    val completedAt = task.completedAt
                    if (completedAt != null && isDateOlderThan24Hours(completedAt)) {
                        // Deletar tarefa antiga em background
                        deleteOldTaskInBackground(task.id)
                    }
                }
            }
        }
    }

    /**
     * Apaga uma tarefa antiga em background sem afetar a UI
     */
    private suspend fun deleteOldTaskInBackground(taskId: Int) {
        try {
            taskRepository.deleteTask(taskId)
            // Log opcional para debug
            println("✅ Tarefa $taskId eliminada automaticamente (concluída há mais de 24h)")
        } catch (e: Exception) {
            println("⚠️ Erro ao eliminar tarefa antiga $taskId: ${e.message}")
        }
    }

    /**
     * Verifica se uma data ISO é mais antiga que 24 horas
     */
    private fun isDateOlderThan24Hours(isoDateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(isoDateString)
            val now = Date()

            val diffInMillis = now.time - date.time
            val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)

            diffInHours >= 24
        } catch (e: Exception) {
            // Se não conseguir parsear, assume que não é antiga
            false
        }
    }

    private fun getCurrentISOTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
}
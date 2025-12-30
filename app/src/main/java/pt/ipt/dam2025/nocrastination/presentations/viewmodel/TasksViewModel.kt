// presentation/viewmodel/TasksViewModel.kt
package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.*

class TasksViewModel  constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                    // Update local list
                    _tasks.value = _tasks.value + result.data
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
                    // Update local list
                    _tasks.value = _tasks.value.map {
                        if (it.id == result.data.id) result.data else it
                    }
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
            val task = _tasks.value.find { it.id == taskId }
            task?.let {
                val updatedTask = it.copy(
                    completed = true,
                    completedAt = getCurrentISOTimestamp()
                )
                updateTask(updatedTask)
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = taskRepository.deleteTask(taskId)) {
                is Result.Success -> {
                    // Remove from local list
                    _tasks.value = _tasks.value.filter { it.id != taskId }
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

    private fun getCurrentISOTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
}
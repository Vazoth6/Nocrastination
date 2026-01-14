package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.Task

class TaskPomodoroCoordinatorViewModel : ViewModel() {

    // Estado da tarefa selecionada para Pomodoro
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    // Flag para navegação para o ecrã Pomodoro
    private val _shouldNavigateToPomodoro = MutableStateFlow(false)
    val shouldNavigateToPomodoro: StateFlow<Boolean> = _shouldNavigateToPomodoro.asStateFlow()

    // Flag que indica se um Pomodoro foi completado
    private val _pomodoroCompleted = MutableStateFlow(false)
    val pomodoroCompleted: StateFlow<Boolean> = _pomodoroCompleted.asStateFlow()

    /**
     * Seleciona uma tarefa para iniciar um Pomodoro
     * @param task Tarefa selecionada pelo utilizador
     */
    fun selectTaskForPomodoro(task: Task) {
        _selectedTask.value = task
        _shouldNavigateToPomodoro.value = true
    }

    /**
     * Deve ser chamado após a navegação para o Pomodoro ser executada
     * Reseta a flag de navegação para evitar navegações múltiplas
     */
    fun onNavigatedToPomodoro() {
        _shouldNavigateToPomodoro.value = false
    }

    /**
     * Marca o Pomodoro como completado e limpa os estados após um delay
     */
    fun completePomodoro() {
        _pomodoroCompleted.value = true
        viewModelScope.launch {
            // Aguarda 1 segundo antes de limpar os estados
            kotlinx.coroutines.delay(1000)
            _pomodoroCompleted.value = false
            _selectedTask.value = null
        }
    }
}
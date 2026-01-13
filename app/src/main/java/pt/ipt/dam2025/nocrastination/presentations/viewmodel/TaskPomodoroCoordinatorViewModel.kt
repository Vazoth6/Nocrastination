package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.Task

class TaskPomodoroCoordinatorViewModel : ViewModel() {

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _shouldNavigateToPomodoro = MutableStateFlow(false)
    val shouldNavigateToPomodoro: StateFlow<Boolean> = _shouldNavigateToPomodoro.asStateFlow()

    private val _pomodoroCompleted = MutableStateFlow(false)
    val pomodoroCompleted: StateFlow<Boolean> = _pomodoroCompleted.asStateFlow()

    fun selectTaskForPomodoro(task: Task) {
        _selectedTask.value = task
        _shouldNavigateToPomodoro.value = true
    }

    fun onNavigatedToPomodoro() {
        _shouldNavigateToPomodoro.value = false
    }

    fun completePomodoro() {
        _pomodoroCompleted.value = true
        viewModelScope.launch {
            // Reset after a delay
            kotlinx.coroutines.delay(1000)
            _pomodoroCompleted.value = false
            _selectedTask.value = null
        }
    }
}
// presentation/viewmodel/PomodoroViewModel.kt
package pt.ipt.dam2025.nocrastination.presentations.viewmodel  // Fixed: presentations → presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.PomodoroSession
import pt.ipt.dam2025.nocrastination.domain.models.SessionType
import pt.ipt.dam2025.nocrastination.domain.repository.PomodoroRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<PomodoroSession>>(emptyList())
    val sessions: StateFlow<List<PomodoroSession>> = _sessions.asStateFlow()

    private val _currentSession = MutableStateFlow<PomodoroSession?>(null)
    val currentSession: StateFlow<PomodoroSession?> = _currentSession.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun startPomodoro(workDuration: Int, taskId: Int? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val newSession = PomodoroSession(
                id = 0,
                sessionType = SessionType.WORK,
                startTime = System.currentTimeMillis(),
                endTime = null,
                durationMinutes = workDuration,
                completed = false,
                taskId = taskId
            )

            when (val result = pomodoroRepository.startSession(newSession)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    loadTodaySessions()
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao iniciar pomodoro"
                }
            }
            _loading.value = false
        }
    }

    fun completePomodoro() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                _loading.value = true
                _error.value = null

                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                val endTime = isoFormat.format(Date())

                when (val result = pomodoroRepository.completeSession(session.id, endTime)) {
                    is Result.Success -> {
                        _currentSession.value = null
                        loadTodaySessions()
                    }
                    is Result.Error -> {
                        _error.value = result.exception.message ?: "Erro ao completar pomodoro"
                    }
                }
                _loading.value = false
            }
        }
    }

    fun loadTodaySessions() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            when (val result = pomodoroRepository.getTodaySessions()) {
                is Result.Success -> {
                    _sessions.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao carregar sessões"
                }
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
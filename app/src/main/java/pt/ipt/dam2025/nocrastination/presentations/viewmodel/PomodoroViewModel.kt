// presentation/viewmodel/PomodoroViewModel.kt
package pt.ipt.dam2025.nocrastination.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class PomodoroViewModel constructor(
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

    // Configurações do usuário
    private val _customWorkDuration = MutableStateFlow(25) // minutos
    val customWorkDuration: StateFlow<Int> = _customWorkDuration.asStateFlow()

    private val _selectedBreakType = MutableStateFlow(BreakType.SHORT) // Tipo de pausa selecionada
    val selectedBreakType: StateFlow<BreakType> = _selectedBreakType.asStateFlow()

    // Estado do ciclo - usando enum em vez de sealed class
    private val _cycleState = MutableStateFlow(CycleState.WORK)
    val cycleState: StateFlow<CycleState> = _cycleState.asStateFlow()

    fun updateCustomWorkDuration(minutes: Int) {
        _customWorkDuration.value = minutes.coerceIn(1, 120) // Limitar entre 1 e 120 minutos
    }

    fun selectBreakType(breakType: BreakType) {
        _selectedBreakType.value = breakType
    }

    fun setCycleState(state: CycleState) {
        _cycleState.value = state
    }

    fun startPomodoro(useCustomDuration: Boolean = true, taskId: Int? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val workDuration = if (useCustomDuration) {
                _customWorkDuration.value
            } else {
                25 // Default
            }

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
                    _cycleState.value = CycleState.WORK
                    loadTodaySessions()
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao iniciar pomodoro"
                }
            }
            _loading.value = false
        }
    }

    fun startBreak(breakType: BreakType, taskId: Int? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val breakDuration = when (breakType) {
                BreakType.SHORT -> 5
                BreakType.LONG -> 15
            }

            val sessionType = when (breakType) {
                BreakType.SHORT -> SessionType.SHORT_BREAK
                BreakType.LONG -> SessionType.LONG_BREAK
            }

            val breakSession = PomodoroSession(
                id = 0,
                sessionType = sessionType,
                startTime = System.currentTimeMillis(),
                endTime = null,
                durationMinutes = breakDuration,
                completed = false,
                taskId = taskId
            )

            when (val result = pomodoroRepository.startSession(breakSession)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    _cycleState.value = CycleState.BREAK
                    loadTodaySessions()
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao iniciar pausa"
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

                        // Iniciar pausa automática após completar trabalho
                        if (_cycleState.value == CycleState.WORK) {
                            startBreak(_selectedBreakType.value)
                        } else {
                            _cycleState.value = CycleState.WORK
                        }

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

    // Usar enum em vez de sealed class com objects
    enum class CycleState {
        WORK, BREAK
    }

    enum class BreakType {
        SHORT, LONG
    }
}
package pt.ipt.dam2025.nocrastination.presentations.viewmodel

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

    // Configurações do utilizador
    private val _customWorkDuration = MutableStateFlow(25) // minutos
    val customWorkDuration: StateFlow<Int> = _customWorkDuration.asStateFlow()

    private val _selectedBreakType = MutableStateFlow(BreakType.SHORT) // Tipo de pausa selecionada
    val selectedBreakType: StateFlow<BreakType> = _selectedBreakType.asStateFlow()

    // Estado do ciclo Pomodoro
    private val _cycleState = MutableStateFlow(CycleState.WORK)
    val cycleState: StateFlow<CycleState> = _cycleState.asStateFlow()

    // UI state para navegação
    private val _showGoToTasksButton = MutableStateFlow(false)
    val showGoToTasksButton: StateFlow<Boolean> = _showGoToTasksButton.asStateFlow()

    private val _completedBreakSession = MutableStateFlow<PomodoroSession?>(null)
    val completedBreakSession: StateFlow<PomodoroSession?> = _completedBreakSession.asStateFlow()

    /**
     * Atualiza a duração personalizada de trabalho
     * @param minutes Minutos (limitado entre 1-120)
     */
    fun updateCustomWorkDuration(minutes: Int) {
        _customWorkDuration.value = minutes.coerceIn(1, 120) // Limitar entre 1 e 120 minutos
    }

    /**
     * Seleciona o tipo de pausa
     * @param breakType SHORT ou LONG
     */
    fun selectBreakType(breakType: BreakType) {
        _selectedBreakType.value = breakType
    }

    /**
     * Define o estado atual do ciclo
     * @param state WORK ou BREAK
     */
    fun setCycleState(state: CycleState) {
        _cycleState.value = state
    }

    /**
     * Inicia uma sessão de trabalho Pomodoro
     * @param useCustomDuration Usar duração personalizada ou padrão (25min)
     * @param taskId ID da tarefa associada (opcional)
     */
    fun startPomodoro(useCustomDuration: Boolean = true, taskId: Int? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            resetGoToTasksButton()

            val workDuration = if (useCustomDuration) {
                _customWorkDuration.value
            } else {
                25 // Padrão clássico do Pomodoro
            }

            val newSession = PomodoroSession(
                id = 0, // 0 indica que é novo (ID será gerado pelo servidor)
                sessionType = SessionType.WORK,
                startTime = System.currentTimeMillis(), // Timestamp atual
                endTime = null,
                durationMinutes = workDuration,
                completed = false,
                taskId = taskId  // Associa a tarefa se fornecida
            )

            when (val result = pomodoroRepository.startSession(newSession)) {
                is Result.Success -> {
                    _currentSession.value = result.data
                    _cycleState.value = CycleState.WORK
                    loadTodaySessions() // Atualiza lista de sessões
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao iniciar pomodoro"
                }
            }
            _loading.value = false
        }
    }

    /**
     * Inicia uma pausa (curta ou longa)
     * @param breakType Tipo de pausa
     * @param taskId ID da tarefa associada (opcional)
     */
    fun startBreak(breakType: BreakType, taskId: Int? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            resetGoToTasksButton()

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
                taskId = taskId  // Passar taskId aqui
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

    /**
     * Completa a sessão Pomodoro atual
     * - Se for trabalho: inicia pausa automática
     * - Se for pausa: mostra botão para tarefas
     */
    fun completePomodoro() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                _loading.value = true
                _error.value = null

                // Formata data para ISO 8601 (UTC)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                val endTime = isoFormat.format(Date())

                when (val result = pomodoroRepository.completeSession(session.id, endTime)) {
                    is Result.Success -> {
                        // Se for uma pausa completada, mostra botão para tarefas
                        if (session.sessionType == SessionType.SHORT_BREAK ||
                            session.sessionType == SessionType.LONG_BREAK) {
                            _showGoToTasksButton.value = true
                            _completedBreakSession.value = session
                        } else {
                            _showGoToTasksButton.value = false
                        }

                        _currentSession.value = null

                        // Fluxo automático: trabalho -> pausa -> trabalho
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

    /**
     * Carrega sessões do dia atual
     */
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

    /**
     * Reseta o botão de navegação para tarefas
     */
    fun resetGoToTasksButton() {
        _showGoToTasksButton.value = false
        _completedBreakSession.value = null
    }

    /**
     * Limpa mensagens de erro
     */
    fun clearError() {
        _error.value = null
    }

    // Enums para estados do ciclo Pomodoro

    enum class CycleState {
        WORK, BREAK
    }

    enum class BreakType {
        SHORT, LONG
    }
}
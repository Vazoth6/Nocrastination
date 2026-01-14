package pt.ipt.dam2025.nocrastination.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.FragmentPomodoroBinding
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.PomodoroViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private val pomodoroViewModel: PomodoroViewModel by viewModel()
    private val tasksViewModel: TasksViewModel by viewModel()

    private var currentTask: Task? = null // Tarefa atual (se fornecida)

    private var timer: CountDownTimer? = null // Temporizador principal
    private var isTimerRunning = false // Estado do temporizador
    private var timeLeftInMillis = 25 * 60 * 1000L // Tempo restante em ms (25min é o padrão)
    private var totalTimeMillis = 25 * 60 * 1000L // Tempo total em ms
    private var sessionCount = 1 // Contador de sessões

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Receber a tarefa dos argumentos utilizando Bundle
        arguments?.let {
            currentTask = it.getParcelable("task")
            currentTask?.let { task ->
                setupTaskForPomodoro(task) // Configurar Pomodoro para tarefa específica
            }
        }

        setupObservers()
        setupClickListeners()
        updateCountDownText()
        updateButtons()
        updateStats()

        pomodoroViewModel.resetGoToTasksButton() // Reset no botão de navegação
    }

    private fun setupTaskForPomodoro(task: Task) {
        // Mostrar título da tarefa
        binding.textTaskTitle.text = "Tarefa: ${task.title}"
        binding.textTaskTitle.visibility = View.VISIBLE

        // Usar estimatedMinutes da tarefa, ou padrão de 25
        val estimatedMinutes = task.estimatedMinutes ?: 25

        // Atualizar duração customizada no ViewModel
        pomodoroViewModel.updateCustomWorkDuration(estimatedMinutes)

        // Definir como trabalho
        pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.WORK)

        // Atualizar texto de duração na UI
        binding.textCustomDuration.text = "${estimatedMinutes}min"

        // Configurar temporizador inicial com a duração da tarefa
        setTimer(estimatedMinutes * 60 * 1000L, "TRABALHO", "WORK")
    }

    private fun setupObservers() {
        // Observar sessão atual
        lifecycleScope.launch {
            pomodoroViewModel.currentSession.collect { session ->
                session?.let {
                    binding.textSessionInfo.text = "Sessão #${session.id}"
                }
            }
        }

        // Observar lista de sessões para atualizar estatísticas
        lifecycleScope.launch {
            pomodoroViewModel.sessions.collect { sessions ->
                updateStats()
            }
        }

        // Observar duração personalizada de trabalho
        lifecycleScope.launch {
            pomodoroViewModel.customWorkDuration.collect { duration ->
                binding.textCustomDuration.text = "${duration}min"
                if (pomodoroViewModel.cycleState.value == PomodoroViewModel.CycleState.WORK) {
                    setTimer(duration * 60 * 1000L, "TRABALHO", "WORK")
                }
            }
        }

        // Observar tipo de pausa selecionada
        lifecycleScope.launch {
            pomodoroViewModel.selectedBreakType.collect { breakType ->
                val breakText = when (breakType) {
                    PomodoroViewModel.BreakType.SHORT -> "PAUSA CURTA (5min)"
                    PomodoroViewModel.BreakType.LONG -> "PAUSA LONGA (15min)"
                }
                binding.textBreakType.text = breakText
            }
        }

        // Observar estado do ciclo (trabalho/pausa)
        lifecycleScope.launch {
            pomodoroViewModel.cycleState.collect { state ->
                when (state) {
                    PomodoroViewModel.CycleState.WORK -> {
                        val duration = pomodoroViewModel.customWorkDuration.value
                        setTimer(duration * 60 * 1000L, "TRABALHO", "WORK")
                        binding.buttonBackToWork.visibility = View.GONE
                        binding.buttonBreakSelector.visibility = View.VISIBLE
                    }
                    PomodoroViewModel.CycleState.BREAK -> {
                        val breakDuration = when (pomodoroViewModel.selectedBreakType.value) {
                            PomodoroViewModel.BreakType.SHORT -> 5
                            PomodoroViewModel.BreakType.LONG -> 15
                        }
                        val breakType = when (pomodoroViewModel.selectedBreakType.value) {
                            PomodoroViewModel.BreakType.SHORT -> "PAUSA CURTA"
                            PomodoroViewModel.BreakType.LONG -> "PAUSA LONGA"
                        }
                        val sessionType = when (pomodoroViewModel.selectedBreakType.value) {
                            PomodoroViewModel.BreakType.SHORT -> "SHORT_BREAK"
                            PomodoroViewModel.BreakType.LONG -> "LONG_BREAK"
                        }
                        setTimer(breakDuration * 60 * 1000L, breakType, sessionType)
                        binding.buttonBackToWork.visibility = View.VISIBLE
                        binding.buttonBreakSelector.visibility = View.GONE
                    }
                }
            }
        }

        // Observar visibilidade do botão "Ir para tarefas"
        lifecycleScope.launch {
            pomodoroViewModel.showGoToTasksButton.collect { showButton ->
                binding.buttonGoToTasks.visibility = if (showButton) View.VISIBLE else View.GONE

                // Ajustar a margem do statsCard baseado na visibilidade do botão
                val layoutParams = binding.statsCard.layoutParams as ConstraintLayout.LayoutParams
                if (showButton) {
                    layoutParams.topToBottom = R.id.buttonGoToTasks
                } else {
                    layoutParams.topToBottom = R.id.breakCard
                }
                binding.statsCard.layoutParams = layoutParams
            }
        }

        // Configurar o clique do botão (FORA do lifecycleScope.launch)
        binding.buttonGoToTasks.setOnClickListener {
            navigateToTasks()
        }
    }

    private fun setupClickListeners() {
        binding.buttonStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.buttonReset.setOnClickListener {
            resetTimer()
        }

        binding.buttonCustomDuration.setOnClickListener {
            showCustomTimeDialog()
        }

        binding.buttonShortBreak.setOnClickListener {
            if (!isTimerRunning) {
                pomodoroViewModel.selectBreakType(PomodoroViewModel.BreakType.SHORT)
                pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.BREAK)
                resetTimer()
            }
        }

        binding.buttonLongBreak.setOnClickListener {
            if (!isTimerRunning) {
                pomodoroViewModel.selectBreakType(PomodoroViewModel.BreakType.LONG)
                pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.BREAK)
                resetTimer()
            }
        }

        binding.buttonBackToWork.setOnClickListener {
            if (!isTimerRunning) {
                pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.WORK)
                resetTimer()
            }
        }

        binding.buttonBreakSelector.setOnClickListener {
            showBreakTypeSelector()
        }
    }

    private fun showCustomTimeDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Definir tempo de trabalho")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER // Apenas números
        input.hint = "Minutos (1-120)"
        input.setText(pomodoroViewModel.customWorkDuration.value.toString()) // Valor atual

        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val minutes = input.text.toString().toIntOrNull() ?: 25
            pomodoroViewModel.updateCustomWorkDuration(minutes)
        }
        builder.setNegativeButton("Cancelar", null)

        builder.show()
    }

    private fun showBreakTypeSelector() {
        val breakTypes = arrayOf("Pausa Curta (5min)", "Pausa Longa (15min)")
        val currentSelection = when (pomodoroViewModel.selectedBreakType.value) {
            PomodoroViewModel.BreakType.SHORT -> 0
            PomodoroViewModel.BreakType.LONG -> 1
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Selecionar tipo de pausa")
            .setSingleChoiceItems(breakTypes, currentSelection) { dialog, which ->
                val selectedType = when (which) {
                    0 -> PomodoroViewModel.BreakType.SHORT
                    else -> PomodoroViewModel.BreakType.LONG
                }
                pomodoroViewModel.selectBreakType(selectedType)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun startTimer() {
        if (pomodoroViewModel.cycleState.value == PomodoroViewModel.CycleState.WORK) {
            startWorkTimer()
        } else {
            startBreakTimer()
        }
    }

    private fun startWorkTimer() {
        val duration = pomodoroViewModel.customWorkDuration.value
        val task = currentTask

        lifecycleScope.launch {
            pomodoroViewModel.startPomodoro(
                useCustomDuration = true,
                taskId = task?.id // Associar tarefa à sessão
            )
        }

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                // Calcular progresso para a ProgressBar
                val progress = ((totalTimeMillis - millisUntilFinished) * 100 / totalTimeMillis).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                isTimerRunning = false
                updateButtons()

                lifecycleScope.launch {
                    pomodoroViewModel.completePomodoro()

                    // Marcar tarefa como completa quando o temporizador acabar
                    currentTask?.let { task ->
                        tasksViewModel.completeTask(task.id)
                    }
                }

                showCompletionNotification()
                updateStats()
            }
        }.start()

        isTimerRunning = true
        updateButtons()
        binding.textSessionInfo.text = "Sessão #${sessionCount}"
    }

    private fun startBreakTimer() {
        val breakType = pomodoroViewModel.selectedBreakType.value
        val task = currentTask

        lifecycleScope.launch {
            pomodoroViewModel.startBreak(breakType, task?.id)
        }

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                val progress = ((totalTimeMillis - millisUntilFinished) * 100 / totalTimeMillis).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                isTimerRunning = false
                updateButtons()

                lifecycleScope.launch {
                    pomodoroViewModel.completePomodoro()
                }

                Toast.makeText(
                    context,
                    " Pausa terminada! Volte ao trabalho.",
                    Toast.LENGTH_LONG
                ).show()

                updateStats()
            }
        }.start()

        isTimerRunning = true
        updateButtons()
        binding.textSessionInfo.text = when (breakType) {
            PomodoroViewModel.BreakType.SHORT -> "Pausa Curta"
            PomodoroViewModel.BreakType.LONG -> "Pausa Longa"
        }
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        updateButtons()
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = totalTimeMillis
        updateCountDownText()
        isTimerRunning = false
        updateButtons()
        binding.progressCircular.progress = 0 // Reset no progresso
    }

    private fun setTimer(milliseconds: Long, timerType: String, sessionType: String) {
        timer?.cancel()
        totalTimeMillis = milliseconds
        timeLeftInMillis = milliseconds
        updateCountDownText()
        isTimerRunning = false
        updateButtons()
        binding.progressCircular.progress = 0

        binding.textTimerType.text = timerType

        // Definir cores conforme tipo de sessão
        when (sessionType) {
            "WORK" -> {
                binding.cardTimerType.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                )
                binding.progressCircular.setIndicatorColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                )
            }
            "SHORT_BREAK" -> {
                binding.cardTimerType.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                )
                binding.progressCircular.setIndicatorColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                )
            }
            "LONG_BREAK" -> {
                binding.cardTimerType.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                )
                binding.progressCircular.setIndicatorColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                )
            }
        }
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.textCountdown.text = timeLeftFormatted
    }

    private fun updateButtons() {
        if (isTimerRunning) {
            binding.buttonStartPause.text = "PAUSAR"
            binding.buttonStartPause.setIconResource(android.R.drawable.ic_media_pause)
            binding.buttonReset.visibility = View.GONE
            binding.buttonCustomDuration.isEnabled = false
            binding.buttonShortBreak.isEnabled = false
            binding.buttonLongBreak.isEnabled = false
            binding.buttonBreakSelector.isEnabled = false
            binding.buttonBackToWork.isEnabled = false
        } else {
            binding.buttonStartPause.text = "INICIAR"
            binding.buttonStartPause.setIconResource(android.R.drawable.ic_media_play)
            binding.buttonReset.visibility = View.VISIBLE
            binding.buttonCustomDuration.isEnabled = true
            binding.buttonShortBreak.isEnabled = true
            binding.buttonLongBreak.isEnabled = true
            binding.buttonBreakSelector.isEnabled = true
            binding.buttonBackToWork.isEnabled = true
        }
    }

    private fun updateStats() {
        lifecycleScope.launch {
            val sessions = pomodoroViewModel.sessions.value
            val todaySessions = sessions.filter {
                // Filtrar sessões de hoje
                true
            }

            binding.textTodayPomodoros.text = todaySessions.size.toString()

            val totalMinutes = todaySessions.sumOf { it.durationMinutes }
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            binding.textTodayFocus.text = if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"

            val completed = todaySessions.count { it.completed }
            val completionRate = if (todaySessions.isNotEmpty()) {
                (completed * 100 / todaySessions.size)
            } else {
                0
            }
            binding.textTodayCompleted.text = "$completionRate%"
        }
    }

    private fun navigateToTasks() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = R.id.tasksFragment
    }

    private fun showCompletionNotification() {
        Toast.makeText(
            context,
            "Tempo de trabalho esgotado! Tarefa marcada como completa.",
            Toast.LENGTH_LONG
        ).show()
        sessionCount++
    }

    private fun getCurrentISOTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel() // Cancelar temporizador para evitar derrame de memória
        _binding = null
    }
}


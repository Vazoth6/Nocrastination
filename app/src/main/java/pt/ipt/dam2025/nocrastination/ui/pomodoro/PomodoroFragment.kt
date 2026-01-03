package pt.ipt.dam2025.nocrastination.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.databinding.FragmentPomodoroBinding
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.PomodoroViewModel

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private val pomodoroViewModel: PomodoroViewModel by viewModel()

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis = 25 * 60 * 1000L // 25 minutos em milissegundos
    private var totalTimeMillis = 25 * 60 * 1000L
    private var sessionCount = 1

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

        setupObservers()
        setupClickListeners()
        updateCountDownText()
        updateButtons()
        updateStats()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            pomodoroViewModel.currentSession.collect { session ->
                session?.let {
                    binding.textSessionInfo.text = "Sessão #${session.id}"
                }
            }
        }

        lifecycleScope.launch {
            pomodoroViewModel.sessions.collect { sessions ->
                updateStats()
            }
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

        binding.buttonShortBreak.setOnClickListener {
            setTimer(5 * 60 * 1000L, "PAUSA CURTA", "SHORT_BREAK")
        }

        binding.buttonLongBreak.setOnClickListener {
            setTimer(15 * 60 * 1000L, "PAUSA LONGA", "LONG_BREAK")
        }

        binding.buttonPomodoro.setOnClickListener {
            setTimer(25 * 60 * 1000L, "POMODORO", "WORK")
        }
    }

    private fun startTimer() {
        // Iniciar sessão no backend
        lifecycleScope.launch {
            pomodoroViewModel.startPomodoro(
                workDuration = (totalTimeMillis / 60000).toInt(),
                taskId = null
            )
        }

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                // Atualizar progresso circular
                val progress = ((totalTimeMillis - millisUntilFinished) * 100 / totalTimeMillis).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                isTimerRunning = false
                updateButtons()

                // Completar a sessão no backend
                lifecycleScope.launch {
                    pomodoroViewModel.completePomodoro()
                }

                // Mostrar notificação
                showCompletionNotification()
                updateStats()
            }
        }.start()

        isTimerRunning = true
        updateButtons()
        binding.textSessionInfo.text = "Sessão #${sessionCount}"
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
        binding.progressCircular.progress = 0
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

        // Atualizar cores baseadas no tipo
        when (sessionType) {
            "WORK" -> {
                binding.cardTimerType.setCardBackgroundColor(resources.getColor(android.R.color.holo_orange_dark, null))
                binding.progressCircular.setIndicatorColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
            "SHORT_BREAK" -> {
                binding.cardTimerType.setCardBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
                binding.progressCircular.setIndicatorColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            "LONG_BREAK" -> {
                binding.cardTimerType.setCardBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
                binding.progressCircular.setIndicatorColor(resources.getColor(android.R.color.holo_blue_dark, null))
            }
        }

        Toast.makeText(context, "Timer definido: $timerType", Toast.LENGTH_SHORT).show()
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
            binding.buttonStartPause.icon = resources.getDrawable(android.R.drawable.ic_media_pause, null)
            binding.buttonReset.visibility = View.GONE
        } else {
            binding.buttonStartPause.text = "INICIAR"
            binding.buttonStartPause.icon = resources.getDrawable(android.R.drawable.ic_media_play, null)
            binding.buttonReset.visibility = View.VISIBLE
        }
    }

    private fun updateStats() {
        lifecycleScope.launch {
            val sessions = pomodoroViewModel.sessions.value
            val todaySessions = sessions.filter {
                // Filtrar sessões de hoje
                true // TODO: Implementar filtro por data
            }

            binding.textTodayPomodoros.text = todaySessions.size.toString()

            val totalMinutes = todaySessions.sumOf { it.durationMinutes }
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            binding.textTodayFocus.text = if (hours > 0) "${hours}h" else "${minutes}m"

            val completed = todaySessions.count { it.completed }
            val completionRate = if (todaySessions.isNotEmpty()) {
                (completed * 100 / todaySessions.size)
            } else {
                0
            }
            binding.textTodayCompleted.text = "$completionRate%"
        }
    }

    private fun showCompletionNotification() {
        Toast.makeText(
            context,
            "🎉 Tempo esgotado! Bom trabalho!",
            Toast.LENGTH_LONG
        ).show()

        // Incrementar contador de sessões
        sessionCount++
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
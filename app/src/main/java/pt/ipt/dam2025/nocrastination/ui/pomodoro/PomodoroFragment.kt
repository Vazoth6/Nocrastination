// ui/pomodoro/PomodoroFragment.kt
package pt.ipt.dam2025.nocrastination.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.databinding.FragmentPomodoroBinding
import pt.ipt.dam2025.nocrastination.presentation.viewmodel.PomodoroViewModel
import java.util.*

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private val pomodoroViewModel: PomodoroViewModel by viewModel()

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis = 25 * 60 * 1000L
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

        lifecycleScope.launch {
            pomodoroViewModel.customWorkDuration.collect { duration ->
                binding.textCustomDuration.text = "${duration}min"
                if (pomodoroViewModel.cycleState.value == PomodoroViewModel.CycleState.WORK) {
                    setTimer(duration * 60 * 1000L, "TRABALHO", "WORK")
                }
            }
        }

        lifecycleScope.launch {
            pomodoroViewModel.selectedBreakType.collect { breakType ->
                val breakText = when (breakType) {
                    PomodoroViewModel.BreakType.SHORT -> "PAUSA CURTA (5min)"
                    PomodoroViewModel.BreakType.LONG -> "PAUSA LONGA (15min)"
                }
                binding.textBreakType.text = breakText
            }
        }

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
                resetTimer() // Reinicia o timer com a nova duração
            }
        }

        binding.buttonLongBreak.setOnClickListener {
            if (!isTimerRunning) {
                pomodoroViewModel.selectBreakType(PomodoroViewModel.BreakType.LONG)
                pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.BREAK)
                resetTimer() // Reinicia o timer com a nova duração
            }
        }

        binding.buttonBackToWork.setOnClickListener {
            if (!isTimerRunning) {
                pomodoroViewModel.setCycleState(PomodoroViewModel.CycleState.WORK)
                resetTimer() // Reinicia o timer com a nova duração
            }
        }

        binding.buttonBreakSelector.setOnClickListener {
            showBreakTypeSelector()
        }
    }

    private fun showCustomTimeDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Definir tempo de trabalho")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Minutos (1-120)"
        input.setText(pomodoroViewModel.customWorkDuration.value.toString())

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

        MaterialAlertDialogBuilder(requireContext())
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

        lifecycleScope.launch {
            pomodoroViewModel.startPomodoro(useCustomDuration = true, taskId = null)
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

        lifecycleScope.launch {
            pomodoroViewModel.startBreak(breakType, null)
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
                    "⏰ Pausa terminada! Volte ao trabalho.",
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
                true // TODO: Implementar filtro por data
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

    private fun showCompletionNotification() {
        Toast.makeText(
            context,
            "🎉 Tempo de trabalho esgotado! Iniciando pausa...",
            Toast.LENGTH_LONG
        ).show()
        sessionCount++
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
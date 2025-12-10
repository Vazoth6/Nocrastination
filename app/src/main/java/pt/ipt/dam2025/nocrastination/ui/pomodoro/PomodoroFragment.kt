package pt.ipt.dam2025.nocrastination.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentPomodoroBinding

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis = 25 * 60 * 1000L // 25 minutos

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

        setupClickListeners()
        updateCountDownText()
        updateButtons()
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
            setTimer(5 * 60 * 1000L, "Pausa Curta")
        }

        binding.buttonLongBreak.setOnClickListener {
            setTimer(15 * 60 * 1000L, "Pausa Longa")
        }

        binding.buttonPomodoro.setOnClickListener {
            setTimer(25 * 60 * 1000L, "Pomodoro")
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()

                // Atualizar progresso do círculo
                val progress = ((25 * 60 * 1000L - millisUntilFinished) * 100 / (25 * 60 * 1000L)).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                isTimerRunning = false
                updateButtons()
                Toast.makeText(context, "Tempo esgotado! Bom trabalho!", Toast.LENGTH_LONG).show()
                playSound() // Adicionar som depois
            }
        }.start()

        isTimerRunning = true
        updateButtons()
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        updateButtons()
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = 25 * 60 * 1000L
        updateCountDownText()
        isTimerRunning = false
        updateButtons()
        binding.progressCircular.progress = 0
    }

    private fun setTimer(milliseconds: Long, timerType: String) {
        timer?.cancel()
        timeLeftInMillis = milliseconds
        updateCountDownText()
        isTimerRunning = false
        updateButtons()
        binding.progressCircular.progress = 0

        binding.textTimerType.text = timerType
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
            binding.buttonStartPause.text = "Pausar"
            binding.buttonReset.visibility = View.GONE
        } else {
            binding.buttonStartPause.text = "Iniciar"
            binding.buttonReset.visibility = View.VISIBLE
        }
    }

    private fun playSound() {
        // Implementar som de notificação depois
        // MediaPlayer.create(context, R.raw.notification_sound).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
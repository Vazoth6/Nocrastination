package pt.ipt.dam2025.nocrastination.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadStatistics() // Carregar estatísticas ao iniciar o fragmento
    }

    // Dados simulados (mock data)
    private fun loadStatistics() {
        val weeklyData = mapOf(
            "Seg" to 120,
            "Ter" to 90,
            "Qua" to 150,
            "Qui" to 180,
            "Sex" to 200,
            "Sáb" to 60,
            "Dom" to 30
        )

        val taskStats = mapOf(
            "Concluídas" to 12,
            "Pendentes" to 5,
            "Atrasadas" to 2
        )

        val priorityStats = mapOf(
            "Alta" to 8,
            "Média" to 10,
            "Baixa" to 15
        )

        // Atualizar a UI com dados
        binding.apply {
            textTotalTasks.text = "0"
            textTotalMinutes.text = "0 min"
            textCurrentStreak.text = "0 dias"

            // Mock de gráficos
            textWeeklyHours.text = "Total da semana: 0 min"

            // Estatísticas de tarefas
            textCompletedTasks.text = taskStats["Concluídas"].toString()
            textPendingTasks.text = taskStats["Pendentes"].toString()
            textOverdueTasks.text = taskStats["Atrasadas"].toString()

            // Estatísticas de prioridade
            textHighPriority.text = priorityStats["Alta"].toString()
            textMediumPriority.text = priorityStats["Média"].toString()
            textLowPriority.text = priorityStats["Baixa"].toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpar binding
    }
}
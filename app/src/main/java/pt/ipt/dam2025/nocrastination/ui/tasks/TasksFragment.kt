package pt.ipt.dam2025.nocrastination.ui.tasks

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.parcel.Parcelize
import pt.ipt.dam2025.nocrastination.TaskDialogFragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentTasksBinding
import pt.ipt.dam2025.nocrastination.ui.tasks.adapter.TaskAdapter
import java.io.Serializable

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Abrir detalhes da tarefa
                Toast.makeText(context, "Clicou na tarefa: ${task.title}", Toast.LENGTH_SHORT).show()
            },
            onTaskEdit = { task ->
                // Editar tarefa
                openEditTaskDialog(task)
            },
            onTaskDelete = { task ->
                // Eliminar tarefa
                showDeleteConfirmation(task)
            },
            onTaskComplete = { task ->
                // Marcar como concluída
                task.isCompleted = !task.isCompleted
                taskAdapter.notifyDataSetChanged()
                Toast.makeText(context, "Tarefa atualizada", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            openAddTaskDialog()
        }

        // Filtros
        binding.chipAll.setOnClickListener { loadTasks() }
        binding.chipPending.setOnClickListener { filterTasksByStatus(false) }
        binding.chipCompleted.setOnClickListener { filterTasksByStatus(true) }
    }

    private fun loadTasks() {
        // Dados mock (substituir por Room Database depois)
        val mockTasks = listOf(
            Task(
                id = 1,
                title = "Estudar para DAM",
                description = "Preparar projeto de aplicação móvel",
                priority = 3,
                estimatedMinutes = 120,
                completedMinutes = 45,
                isCompleted = false,
                dueDate = "2025-12-15"
            ),
            Task(
                id = 2,
                title = "Fazer exercício físico",
                description = "30 minutos de cardio",
                priority = 2,
                estimatedMinutes = 30,
                completedMinutes = 0,
                isCompleted = false,
                dueDate = "2025-12-10"
            ),
            Task(
                id = 3,
                title = "Ler livro",
                description = "Capítulo 5 do livro de Kotlin",
                priority = 1,
                estimatedMinutes = 60,
                completedMinutes = 60,
                isCompleted = true,
                dueDate = "2025-12-05"
            )
        )

        taskAdapter.submitList(mockTasks)

        // Mostrar/ocultar empty state
        binding.emptyState.root.visibility = if (mockTasks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterTasksByStatus(isCompleted: Boolean) {
        // Implementar filtro real depois
        loadTasks()
    }

    private fun openAddTaskDialog() {
        val dialog = TaskDialogFragment.newInstance()
        dialog.show(parentFragmentManager, "TaskDialogFragment")
    }

    private fun openEditTaskDialog(task: Task) {
        val dialog = TaskDialogFragment.newInstance(task)
        dialog.show(parentFragmentManager, "TaskDialogFragment")
    }

    private fun showDeleteConfirmation(task: Task) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarefa")
            .setMessage("Tem a certeza que deseja eliminar a tarefa '${task.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                Toast.makeText(context, "Tarefa eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Parcelize
    data class Task(
        val id: Long,
        val title: String,
        val description: String?,
        val priority: Int, // 1=baixa, 2=média, 3=alta
        val estimatedMinutes: Int,
        var completedMinutes: Int,
        var isCompleted: Boolean,
        val dueDate: String?
    ): Parcelable, Serializable
}
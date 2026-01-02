package pt.ipt.dam2025.nocrastination.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.TaskDialogFragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentTasksBinding
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import pt.ipt.dam2025.nocrastination.ui.tasks.adapter.TaskAdapter

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    // CORREÇÃO: Use viewModel() do Koin ao invés de activityViewModels()
    private val viewModel: TasksViewModel by viewModel()

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

        Log.d("TasksFragment", "onViewCreated - Inicializando")

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Carregar tarefas ao iniciar
        viewModel.loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                openTaskDialog(task.id)
            },
            onCompleteClick = { taskId ->
                viewModel.completeTask(taskId)
            },
            onEditClick = { task ->
                openTaskDialog(task.id)
            },
            onDeleteClick = { taskId ->
                showDeleteConfirmation(taskId)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collectLatest { tasks ->
                Log.d("TasksFragment", "Tasks atualizadas: ${tasks.size} tarefas")
                taskAdapter.submitList(tasks)

                // Mostrar/ocultar estado vazio
                binding.emptyState.root.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("TasksFragment", "Erro: $errorMessage")
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            openTaskDialog(null)
        }

        // Filtros por chip
        binding.chipAll.setOnClickListener {
            // TODO: Implementar filtro
            Toast.makeText(context, "Mostrar todas as tarefas", Toast.LENGTH_SHORT).show()
        }

        binding.chipPending.setOnClickListener {
            // TODO: Implementar filtro
            Toast.makeText(context, "Mostrar tarefas pendentes", Toast.LENGTH_SHORT).show()
        }

        binding.chipCompleted.setOnClickListener {
            // TODO: Implementar filtro
            Toast.makeText(context, "Mostrar tarefas concluídas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTaskDialog(taskId: Int?) {
        val dialog = TaskDialogFragment.newInstance(taskId)
        dialog.show(parentFragmentManager, "TaskDialog")
    }

    private fun showDeleteConfirmation(taskId: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarefa")
            .setMessage("Tem a certeza que deseja eliminar esta tarefa?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteTask(taskId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
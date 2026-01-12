package pt.ipt.dam2025.nocrastination.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.TaskDialogFragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentTasksBinding
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.UIEvent
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import pt.ipt.dam2025.nocrastination.ui.tasks.adapter.TaskAdapter

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

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
        setupToolbar()

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
            onUncompleteClick = { taskId -> // NOVO: para desmarcar
                viewModel.uncompleteTask(taskId)
            },
            onEditClick = { task ->
                openTaskDialog(task.id)
            },
            onDeleteClick = { taskId ->
                showDeleteConfirmation(taskId)
            },
            onStartPomodoro = { task ->
                navigateToPomodoroWithTask(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun navigateToPomodoroWithTask(task: Task) {
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(
            R.id.action_tasksFragment_to_pomodoroFragment,
            bundle
        )
    }

    private fun setupObservers() {
        // Observar tarefas
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collectLatest { tasks ->
                Log.d("TasksFragment", "Tasks atualizadas: ${tasks.size} tarefas")
                taskAdapter.submitList(tasks)

                binding.emptyState.root.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Observar loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observar erros
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("TasksFragment", "Erro: $errorMessage")
                    viewModel.clearError()
                }
            }
        }

        // Observar eventos UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is UIEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is UIEvent.ShowSnackbar -> {
                        // Se quiser usar Snackbar em vez de Toast
                        // Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    UIEvent.NavigateBack -> {
                        // Navegar para trás se necessário
                    }
                }
            }
        }
    }

    private fun setupChips() {
        // Configurar seleção única
        binding.chipGroup.isSingleSelection = true

        // Listener para o chip "Todas"
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(TasksViewModel.FilterType.ALL)
        }

        // Listener para o chip "Concluídas"
        binding.chipCompleted.setOnClickListener {
            viewModel.setFilter(TasksViewModel.FilterType.COMPLETED)
        }
    }

    private fun updateChipSelection(filterType: TasksViewModel.FilterType) {
        when (filterType) {
            is TasksViewModel.FilterType.ALL -> {
                binding.chipAll.isChecked = true
                binding.chipCompleted.isChecked = false
            }
            is TasksViewModel.FilterType.COMPLETED -> {
                binding.chipAll.isChecked = false
                binding.chipCompleted.isChecked = true
            }
        }
    }

    private fun updateEmptyStateMessage() {
        val filterType = viewModel.filterType.value
        val emptyText = when (filterType) {
            is TasksViewModel.FilterType.ALL -> "Nenhuma tarefa encontrada\nCrie uma nova tarefa para começar!"
            is TasksViewModel.FilterType.COMPLETED -> "Nenhuma tarefa concluída\nConclua algumas tarefas para vê-las aqui!"
            else -> "Nenhuma tarefa encontrada"
        }

        binding.emptyState.textEmptyMessage.text = emptyText
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            openTaskDialog(null)
        }

        binding.chipAll.setOnClickListener {
            // TODO: Implementar filtro
            Toast.makeText(context, "Mostrar todas as tarefas", Toast.LENGTH_SHORT).show()
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

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_tasks)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_focus_locations -> {
                    findNavController().navigate(
                        R.id.action_tasksFragment_to_focusLocationsFragment
                    )
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
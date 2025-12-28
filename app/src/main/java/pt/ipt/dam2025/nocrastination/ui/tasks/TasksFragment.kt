package pt.ipt.dam2025.nocrastination.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.TaskDialogFragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentTasksBinding
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel  // Fixed: presentations → presentation
import pt.ipt.dam2025.nocrastination.ui.tasks.adapter.TaskAdapter

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by activityViewModels()
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
        setupObservers()
        setupClickListeners()

        // Load tasks
        viewModel.loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Show task details or edit dialog
                val dialog = TaskDialogFragment.newInstance(task.id)
                dialog.show(parentFragmentManager, "TaskDialog")
            },
            onCompleteClick = { taskId ->
                viewModel.completeTask(taskId)
            },
            onEditClick = { task ->
                val dialog = TaskDialogFragment.newInstance(task.id)
                dialog.show(parentFragmentManager, "TaskDialog")
            },
            onDeleteClick = { taskId ->
                viewModel.deleteTask(taskId)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {
        // Collect tasks from StateFlow
        lifecycleScope.launch {
            viewModel.tasks.collectLatest { tasks ->
                taskAdapter.submitList(tasks)

                // Show/hide empty state
                if (tasks.isEmpty()) {
                    binding.emptyState.root.visibility = View.VISIBLE
                    binding.recyclerViewTasks.visibility = View.GONE
                } else {
                    binding.emptyState.root.visibility = View.GONE
                    binding.recyclerViewTasks.visibility = View.VISIBLE
                }
            }
        }

        // Collect loading state
        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                // Show/hide progress bar
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Collect error state
        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    // Clear error after showing
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            val dialog = TaskDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "TaskDialog")
        }

        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show all tasks (reset filter)
                lifecycleScope.launch {
                    viewModel.tasks.collectLatest { tasks ->
                        taskAdapter.submitList(tasks)
                    }
                }
            }
        }

        binding.chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show only pending tasks
                lifecycleScope.launch {
                    viewModel.tasks.collectLatest { tasks ->
                        val pendingTasks = tasks.filter { !it.completed }
                        taskAdapter.submitList(pendingTasks)
                    }
                }
            }
        }

        binding.chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show only completed tasks
                lifecycleScope.launch {
                    viewModel.tasks.collectLatest { tasks ->
                        val completedTasks = tasks.filter { it.completed }
                        taskAdapter.submitList(completedTasks)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
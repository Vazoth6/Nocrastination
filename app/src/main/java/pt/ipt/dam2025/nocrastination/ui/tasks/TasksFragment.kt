// TasksFragment.kt (opção alternativa se o Koin não funcionar)
package pt.ipt.dam2025.nocrastination.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.TaskDialogFragment
import pt.ipt.dam2025.nocrastination.databinding.FragmentTasksBinding
import pt.ipt.dam2025.nocrastination.di.TasksViewModelFactory
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import pt.ipt.dam2025.nocrastination.ui.tasks.adapter.TaskAdapter

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    // Injetar dependências com Koin
    private val taskRepository: TaskRepository by inject()

    // Criar ViewModel com Factory
    private val viewModel: TasksViewModel by lazy {
        ViewModelProvider(
            this,
            TasksViewModelFactory(taskRepository)
        )[TasksViewModel::class.java]
    }

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ... resto do código permanece igual
}
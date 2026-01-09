package pt.ipt.dam2025.nocrastination

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.ipt.dam2025.nocrastination.databinding.TaskDialogBinding
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.TaskPriority
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

class TaskDialogFragment : DialogFragment() {

    private var _binding: TaskDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by viewModel()

    private var task: Task? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val ARG_TASK_ID = "task_id"

        fun newInstance(taskId: Int? = null): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            taskId?.let {
                val args = Bundle()
                args.putInt(ARG_TASK_ID, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val taskId = it.getInt(ARG_TASK_ID, -1)
            if (taskId != -1) {
                // Find task in ViewModel's list
                viewModel.tasks.value.find { task -> task.id == taskId }?.let { foundTask ->
                    task = foundTask
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TaskDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()

        if (task != null) {
            loadTaskData()
        }
    }

    private fun setupUI() {
        // Configurar o spinner de prioridade
        val priorities = arrayOf("Baixa", "Média", "Alta")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        // Definir título do diálogo
        binding.textTitle.text = if (task == null) "Nova Tarefa" else "Editar Tarefa"

        binding.editEstimatedMinutes.visibility = View.GONE
        binding.editCompletedMinutes.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.buttonSave.setOnClickListener {
            saveTask()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSetDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSetTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun loadTaskData() {
        task?.let { task ->
            binding.editTitle.setText(task.title)
            binding.editDescription.setText(task.description)

            // Prioridade
            val priorityIndex = when (task.priority) {
                TaskPriority.LOW -> 0
                TaskPriority.MEDIUM -> 1
                TaskPriority.HIGH -> 2
            }
            binding.spinnerPriority.setSelection(priorityIndex)

            // Data de vencimento
            task.dueDate?.let { dueDateString ->
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = dateFormat.parse(dueDateString)
                    date?.let {
                        calendar.time = date
                        updateDateTimeDisplay()
                    }
                } catch (e: Exception) {
                    // If parsing fails, try another format
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val date = dateFormat.parse(dueDateString)
                        date?.let {
                            calendar.time = date
                            updateDateTimeDisplay()
                        }
                    } catch (e2: Exception) {
                        binding.textSelectedDateTime.text = "Data inválida"
                    }
                }
            }
        }
    }

    private fun saveTask() {
        val title = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val priorityIndex = binding.spinnerPriority.selectedItemPosition
        val priority = when (priorityIndex) {
            0 -> TaskPriority.LOW
            1 -> TaskPriority.MEDIUM
            2 -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }

        // Validações
        if (title.isEmpty()) {
            binding.editTitle.error = "O título é obrigatório"
            return
        }

        // Formatar a data corretamente
        val dueDate = if (binding.textSelectedDateTime.text != "Não definida" &&
            binding.textSelectedDateTime.text.isNotEmpty()) {
            try {
                // Converter de "dd/MM/yyyy HH:mm" para ISO 8601
                val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = displayFormat.parse(binding.textSelectedDateTime.text.toString())

                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")

                isoFormat.format(date ?: Date())
            } catch (e: Exception) {
                Log.e("TaskDialog", "Erro ao formatar data: ${e.message}")
                null
            }
        } else {
            null
        }

        Log.d("TaskDialog", "📅 Data formatada: $dueDate")

        lifecycleScope.launch {
            if (task == null) {
                // Create new task
                val newTask = Task(
                    id = 0, // Will be set by server
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    completed = false,
                    completedAt = null,
                    createdAt = getCurrentISOTimestamp(),
                    updatedAt = getCurrentISOTimestamp(),
                    estimatedMinutes = null // Você pode adicionar um campo para isso se necessário
                )

                Log.d("TaskDialog", "📤 Enviando tarefa: $newTask")
                viewModel.createTask(newTask)

                // Aguarde um momento antes de recarregar
                delay(500) // Pequeno delay para garantir que o servidor processou
                viewModel.loadTasks() // Força o reload

                Toast.makeText(context, "Tarefa criada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                // Update existing task
                val updatedTask = task!!.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    updatedAt = getCurrentISOTimestamp()
                )

                viewModel.updateTask(updatedTask)
                Toast.makeText(context, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
            }

            dismiss()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                updateDateTimeDisplay()
            },
            year,
            month,
            day
        ).show()
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                updateDateTimeDisplay()
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.textSelectedDateTime.text = dateFormat.format(calendar.time)
    }

    private fun getCurrentISOTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    override fun onStart() {
        super.onStart()
        // Definir tamanho do diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
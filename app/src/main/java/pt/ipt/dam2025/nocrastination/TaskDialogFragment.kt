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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private var taskId: Int? = null
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
            taskId = it.getInt(ARG_TASK_ID, -1).takeIf { id -> id != -1 }
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

        // Carregar dados da tarefa se estiver editando
        taskId?.let { id ->
            loadTaskData(id)
        }
    }

    private fun setupUI() {
        // Configurar o spinner de prioridade
        val priorities = arrayOf("Baixa", "Média", "Alta")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        // Definir título do diálogo
        binding.textTitle.text = if (taskId == null) "Nova Tarefa" else "Editar Tarefa"

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

    private fun loadTaskData(taskId: Int) {
        lifecycleScope.launch {
            // Buscar a tarefa pelo ID usando o ViewModel
            val task = viewModel.tasks.value.find { it.id == taskId }

            if (task != null) {
                populateTaskData(task)
            } else {
                // Se não encontrar na lista atual, tenta buscar do repositório
                viewModel.loadTasks() // Recarrega as tarefas
                delay(500) // Pequeno delay

                val reloadedTask = viewModel.tasks.value.find { it.id == taskId }
                if (reloadedTask != null) {
                    populateTaskData(reloadedTask)
                } else {
                    Toast.makeText(context, "Tarefa não encontrada", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    private fun populateTaskData(task: Task) {
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
                parseAndSetDateTime(dueDateString)
            } catch (e: Exception) {
                Log.e("TaskDialog", "Erro ao parsear data: ${e.message}")
                binding.textSelectedDateTime.text = "Data inválida"
            }
        }

        // Tempo estimado (se existir)
        task.estimatedMinutes?.let {
            binding.editEstimatedMinutes.setText(it.toString())
        }
    }

    private fun parseAndSetDateTime(dueDateString: String) {
        try {
            // Primeiro tenta o formato ISO
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dueDateString)

            if (date != null) {
                calendar.time = date
                updateDateTimeDisplay()
                return
            }
        } catch (e: Exception) {

        }

        // Tenta outros formatos comuns
        try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = format.parse(dueDateString)
            date?.let {
                calendar.time = date
                updateDateTimeDisplay()
            }
        } catch (e: Exception) {
            Log.e("TaskDialog", "Formato de data não reconhecido: $dueDateString")
            binding.textSelectedDateTime.text = "Data inválida"
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
            binding.textSelectedDateTime.text.isNotEmpty() &&
            binding.textSelectedDateTime.text != "Data inválida") {

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

        Log.d("TaskDialog", "Data formatada: $dueDate")

        lifecycleScope.launch {
            if (taskId == null) {
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
                    estimatedMinutes = null
                )

                Log.d("TaskDialog", "A enviar nova tarefa: $newTask")
                viewModel.createTask(newTask)
                Toast.makeText(context, "Tarefa criada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                // Update existing task - primeiro buscar a tarefa atual
                val currentTask = viewModel.tasks.value.find { it.id == taskId }

                if (currentTask != null) {
                    val updatedTask = currentTask.copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        updatedAt = getCurrentISOTimestamp()
                    )

                    Log.d("TaskDialog", "A atualizar tarefa: $updatedTask")
                    viewModel.updateTask(updatedTask)
                    Toast.makeText(context, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro: Tarefa não encontrada", Toast.LENGTH_SHORT).show()
                }
            }

            // Aguardar um momento para garantir que o servidor processou
            delay(500)
            // Recarregar a lista de tarefas
            viewModel.loadTasks()

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
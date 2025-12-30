package pt.ipt.dam2025.nocrastination

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.databinding.TaskDialogBinding
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.TaskPriority
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

class TaskDialogFragment : DialogFragment() {

    private var _binding: TaskDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by activityViewModels()

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

        val dueDate = binding.textSelectedDateTime.text.toString().takeIf {
            it != "Não definida" && it.isNotEmpty()
        }?.let {
            // Convert from "dd/MM/yyyy HH:mm" to ISO format
            val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = displayFormat.parse(it)
            isoFormat.format(date ?: Date())
        }

        // Validações
        if (title.isEmpty()) {
            binding.editTitle.error = "O título é obrigatório"
            return
        }

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
                    updatedAt = getCurrentISOTimestamp()
                )

                viewModel.createTask(newTask)
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

/*package pt.ipt.dam2025.nocrastination

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import pt.ipt.dam2025.nocrastination.databinding.TaskDialogBinding
import pt.ipt.dam2025.nocrastination.ui.tasks.TasksFragment
import java.text.SimpleDateFormat
import java.util.*

class TaskDialogFragment : DialogFragment() {

    private var _binding: TaskDialogBinding? = null
    private val binding get() = _binding!!

    private var task: TasksFragment.Task? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: TasksFragment.Task? = null): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            task?.let {
                val args = Bundle()
                args.putSerializable(ARG_TASK, task)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getSerializable(ARG_TASK) as? TasksFragment.Task
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
            binding.editDescription.setText(task.description ?: "")

            // Prioridade (1=Baixa, 2=Média, 3=Alta)
            val priorityIndex = when (task.priority) {
                1 -> 0 // Baixa
                2 -> 1 // Média
                3 -> 2 // Alta
                else -> 0
            }
            binding.spinnerPriority.setSelection(priorityIndex)

            binding.editEstimatedMinutes.setText(task.estimatedMinutes.toString())
            binding.editCompletedMinutes.setText(task.completedMinutes.toString())

            if (task.dueDate != null) {
                binding.textSelectedDateTime.text = task.dueDate
            }
        }
    }

    private fun saveTask() {
        val title = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val priorityIndex = binding.spinnerPriority.selectedItemPosition
        val priority = priorityIndex + 1 // 1=Baixa, 2=Média, 3=Alta
        val estimatedMinutes = binding.editEstimatedMinutes.text.toString().toIntOrNull() ?: 25
        val completedMinutes = binding.editCompletedMinutes.text.toString().toIntOrNull() ?: 0
        val dueDate = binding.textSelectedDateTime.text.toString().takeIf {
            it != "Não definida" && it.isNotEmpty()
        }

        // Validações
        if (title.isEmpty()) {
            binding.editTitle.error = "O título é obrigatório"
            return
        }

        if (estimatedMinutes <= 0) {
            Toast.makeText(context, "O tempo estimado deve ser maior que 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (completedMinutes < 0) {
            Toast.makeText(context, "O tempo completado não pode ser negativo", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar ou atualizar a tarefa
        val updatedTask = if (task == null) {
            TasksFragment.Task(
                id = Date().time, // ID temporário
                title = title,
                description = if (description.isNotEmpty()) description else null,
                priority = priority,
                estimatedMinutes = estimatedMinutes,
                completedMinutes = completedMinutes,
                isCompleted = completedMinutes >= estimatedMinutes,
                dueDate = dueDate
            )
        } else {
            task!!.copy(
                title = title,
                description = if (description.isNotEmpty()) description else null,
                priority = priority,
                estimatedMinutes = estimatedMinutes,
                completedMinutes = completedMinutes,
                isCompleted = completedMinutes >= estimatedMinutes,
                dueDate = dueDate
            )
        }

        // Aqui deverias passar a tarefa de volta para o TasksFragment
        // Por enquanto, apenas mostramos uma mensagem
        Toast.makeText(
            context,
            if (task == null) "Tarefa criada com sucesso!" else "Tarefa atualizada!",
            Toast.LENGTH_SHORT
        ).show()

        // Fechar o diálogo
        dismiss()
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
}*/
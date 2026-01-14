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

// Fragmento de diálogo para criar ou editar tarefas
class TaskDialogFragment : DialogFragment() {

    // Binding para o layout do diálogo
    private var _binding: TaskDialogBinding? = null
    private val binding get() = _binding!!

    // ViewModel para gerir tarefas
    private val viewModel: TasksViewModel by viewModel()

    // Variáveis para controlar estado
    private var taskId: Int? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val ARG_TASK_ID = "task_id"

        // Metodo para criar nova instância do diálogo
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

    // Metodo chamado na criação do fragmento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getInt(ARG_TASK_ID, -1).takeIf { id -> id != -1 }
        }
    }

    // Criar a vista do fragmento
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TaskDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Metodo chamado após a view ser criada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()

        // Carregar dados da tarefa se estiver em modo de edição
        taskId?.let { id ->
            loadTaskData(id)
        }
    }

    // Configurar elementos da interface de utilizador
    private fun setupUI() {
        // Configurar spinner de prioridade
        val priorities = arrayOf("Baixa", "Média", "Alta")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        // Definir título do diálogo
        binding.textTitle.text = if (taskId == null) "Nova Tarefa" else "Editar Tarefa"

        // Ocultar campos não usados
        binding.editEstimatedMinutes.visibility = View.GONE
        binding.editCompletedMinutes.visibility = View.GONE
    }

    // Configurar listeners para botões
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

    // Carregar dados de uma tarefa existente
    private fun loadTaskData(taskId: Int) {
        lifecycleScope.launch {
            // Procurar tarefa na lista atual
            val task = viewModel.tasks.value.find { it.id == taskId }

            if (task != null) {
                populateTaskData(task)
            } else {
                // Se não encontrar, recarrega a lista
                viewModel.loadTasks()
                delay(500) // Pequena pausa

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

    // Preencher campos com dados da tarefa
    private fun populateTaskData(task: Task) {
        binding.editTitle.setText(task.title)
        binding.editDescription.setText(task.description)

        // Definir prioridade no spinner
        val priorityIndex = when (task.priority) {
            TaskPriority.LOW -> 0
            TaskPriority.MEDIUM -> 1
            TaskPriority.HIGH -> 2
        }
        binding.spinnerPriority.setSelection(priorityIndex)

        // Definir data de vencimento
        task.dueDate?.let { dueDateString ->
            try {
                parseAndSetDateTime(dueDateString)
            } catch (e: Exception) {
                Log.e("TaskDialog", "Erro ao parsear data: ${e.message}")
                binding.textSelectedDateTime.text = "Data inválida"
            }
        }

        // Definir tempo estimado (se existir)
        task.estimatedMinutes?.let {
            binding.editEstimatedMinutes.setText(it.toString())
        }
    }

    // Analisar string de data e definir no calendário
    private fun parseAndSetDateTime(dueDateString: String) {
        try {
            // Tentar formato ISO
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

        // Tentar formato comum
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

    // Guardar/atualizar tarefa
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

        // Formatar data para ISO 8601
        val dueDate = if (binding.textSelectedDateTime.text != "Não definida" &&
            binding.textSelectedDateTime.text.isNotEmpty() &&
            binding.textSelectedDateTime.text != "Data inválida") {

            try {
                // Converter data para ISO 8601
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
                    id = 0, // Definido pelo servidor
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
                // Atualizar tarefa existente
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

            // Aguardar processamento do servidor
            delay(500)
            // Recarregar lista de tarefas
            viewModel.loadTasks()

            dismiss()
        }
    }

    // Mostrar seletor de data
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

    // Mostrar seletor de hora
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

    // Atualizar display da data/hora selecionada
    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.textSelectedDateTime.text = dateFormat.format(calendar.time)
    }

    // Obter timestamp atual em formato ISO
    private fun getCurrentISOTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    // Configurar dimensões do diálogo
    override fun onStart() {
        super.onStart()
        // Definir tamanho do diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // Limpar binding quando a view é destruída
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
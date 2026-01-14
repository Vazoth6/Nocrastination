package pt.ipt.dam2025.nocrastination.ui.tasks.adapter

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2025.nocrastination.databinding.ItemTaskBinding
import pt.ipt.dam2025.nocrastination.domain.models.Task
import pt.ipt.dam2025.nocrastination.domain.models.TaskPriority
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCompleteClick: (Int) -> Unit,
    private val onUncompleteClick: (Int) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onStartPomodoro: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task) // Ligar dados à view
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            // Definir texto dos elementos da interface
            binding.textTaskTitle.text = task.title
            binding.textTaskDescription.text = task.description
            binding.textDueDate.text = formatDate(task.dueDate)

            // Configurar chip de prioridade
            binding.chipPriority.text = when (task.priority) {
                TaskPriority.LOW -> "Baixa"
                TaskPriority.MEDIUM -> "Média"
                TaskPriority.HIGH -> "Alta"
            }

            // Botão para iniciar Pomodoro
            binding.buttonPomodoro.setOnClickListener {
                onStartPomodoro(task)
            }

            // Definir cor do chip baseado na prioridade
            val priorityColor = when (task.priority) {
                TaskPriority.LOW -> R.color.holo_green_light
                TaskPriority.MEDIUM -> R.color.holo_orange_light
                TaskPriority.HIGH -> R.color.holo_red_light
            }
            binding.chipPriority.setChipBackgroundColorResource(priorityColor)

            // Estado de conclusão
            binding.checkBoxCompleted.isChecked = task.completed

            // Listeners de clique
            binding.cardTask.setOnClickListener {
                onTaskClick(task)
            }

            // Listener para checkbox de conclusão
            binding.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                // Remover o listener temporariamente para evitar loops infinitos
                binding.checkBoxCompleted.setOnCheckedChangeListener(null)

                // Se o estado mudou, chama a função apropriada
                if (isChecked != task.completed) {
                    if (isChecked) {
                        onCompleteClick(task.id) // Marcar como concluída
                    } else {
                        onUncompleteClick(task.id) // Desmarca
                    }
                }

                // Restaurar o listener
                binding.checkBoxCompleted.setOnCheckedChangeListener { _, newIsChecked ->
                    if (newIsChecked != task.completed) {
                        if (newIsChecked) {
                            onCompleteClick(task.id)
                        } else {
                            onUncompleteClick(task.id)
                        }
                    }
                }
            }

            // Botão de editar
            binding.buttonEdit.setOnClickListener {
                onEditClick(task)
            }

            // Botão de eliminar
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(task.id)
            }
        }

        private fun formatDate(dateString: String?): String {
            return if (dateString.isNullOrEmpty()) {
                "Sem data" // Mensagem padrão se não houver data
            } else {
                try {
                    // Formatar data ISO 8601 para formato legível
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = inputFormat.parse(dateString)

                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    outputFormat.format(date ?: Date())
                } catch (e: Exception) {
                    "Data inválida" // Em caso de erro no parsing
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id // Comparar por ID
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem // Comparar todos os campos (data class)
        }
    }
}
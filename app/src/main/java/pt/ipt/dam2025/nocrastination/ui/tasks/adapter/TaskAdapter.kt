// adapters/TaskAdapter.kt
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
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Int) -> Unit
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
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textTaskTitle.text = task.title
            binding.textTaskDescription.text = task.description
            binding.textDueDate.text = formatDate(task.dueDate)

            // Priority chip
            binding.chipPriority.text = when (task.priority) {
                TaskPriority.LOW -> "Baixa"
                TaskPriority.MEDIUM -> "Média"
                TaskPriority.HIGH -> "Alta"
            }

            // Set chip color based on priority
            val priorityColor = when (task.priority) {
                TaskPriority.LOW -> R.color.holo_green_light
                TaskPriority.MEDIUM -> R.color.holo_orange_light
                TaskPriority.HIGH -> R.color.holo_red_light
            }
            binding.chipPriority.setChipBackgroundColorResource(priorityColor)

            // Completion status
            binding.checkBoxCompleted.isChecked = task.completed

            // Click listeners
            binding.cardTask.setOnClickListener {
                onTaskClick(task)
            }

            binding.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !task.completed) {
                    onCompleteClick(task.id)
                }
            }

            binding.buttonEdit.setOnClickListener {
                onEditClick(task)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(task.id)
            }
        }

        private fun formatDate(dateString: String?): String {
            return if (dateString.isNullOrEmpty()) {
                "Sem data"
            } else {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = inputFormat.parse(dateString)

                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    outputFormat.format(date ?: Date())
                } catch (e: Exception) {
                    "Data inválida"
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
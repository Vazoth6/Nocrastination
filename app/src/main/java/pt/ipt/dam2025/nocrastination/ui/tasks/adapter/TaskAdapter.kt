package pt.ipt.dam2025.nocrastination.ui.tasks.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2025.nocrastination.R
import pt.ipt.dam2025.nocrastination.databinding.ItemTaskBinding
import pt.ipt.dam2025.nocrastination.ui.tasks.TasksFragment

class TaskAdapter(
    private val onTaskClick: (TasksFragment.Task) -> Unit,
    private val onTaskEdit: (TasksFragment.Task) -> Unit,
    private val onTaskDelete: (TasksFragment.Task) -> Unit,
    private val onTaskComplete: (TasksFragment.Task) -> Unit
) : ListAdapter<TasksFragment.Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TasksFragment.Task) {
            binding.apply {
                textTaskTitle.text = task.title
                textTaskDescription.text = task.description ?: "Sem descrição"
                textDueDate.text = task.dueDate ?: "Sem data"

                // Progresso do Pomodoro
                textProgress.text = "${task.completedMinutes}/${task.estimatedMinutes} min"
                progressBar.max = task.estimatedMinutes
                progressBar.progress = task.completedMinutes

                // Prioridade
                val priorityText = when (task.priority) {
                    3 -> "Alta"
                    2 -> "Média"
                    else -> "Baixa"
                }
                chipPriority.text = priorityText

                // Cor da prioridade
                val priorityColor = when (task.priority) {
                    3 -> R.color.priority_high
                    2 -> R.color.priority_medium
                    else -> R.color.priority_low
                }
                chipPriority.setChipBackgroundColorResource(priorityColor)

                // Tarefa concluída
                if (task.isCompleted) {
                    textTaskTitle.paintFlags = textTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textTaskDescription.paintFlags = textTaskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    checkBoxCompleted.isChecked = true
                } else {
                    textTaskTitle.paintFlags = textTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textTaskDescription.paintFlags = textTaskDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    checkBoxCompleted.isChecked = false
                }

                // Listeners
                checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != task.isCompleted) {
                        onTaskComplete(task)
                    }
                }

                buttonEdit.setOnClickListener {
                    onTaskEdit(task)
                }

                buttonDelete.setOnClickListener {
                    onTaskDelete(task)
                }

                root.setOnClickListener {
                    onTaskClick(task)
                }
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<TasksFragment.Task>() {
    override fun areItemsTheSame(oldItem: TasksFragment.Task, newItem: TasksFragment.Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TasksFragment.Task, newItem: TasksFragment.Task): Boolean {
        return oldItem == newItem
    }
}
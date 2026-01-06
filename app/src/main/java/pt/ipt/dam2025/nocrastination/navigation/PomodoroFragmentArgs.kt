package pt.ipt.dam2025.nocrastination.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pt.ipt.dam2025.nocrastination.domain.models.Task

@Parcelize
data class PomodoroFragmentArgs(
    val taskId: Int = 0,
    val taskTitle: String = "",
    val estimatedMinutes: Int = 25,
    val task: Task? = null
) : Parcelable
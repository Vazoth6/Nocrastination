package pt.ipt.dam2025.nocrastination.domain.models

import java.time.LocalDate

data class DailyStat(
    val id: Int,
    val date: LocalDate,
    val tasksCompleted: Int,
    val tasksCreated: Int,
    val totalPomodoroSessions: Int,
    val totalWorkMinutes: Int,
    val totalBreakMinutes: Int
)

package pt.ipt.dam2025.nocrastination.domain.models

import java.time.LocalDate

/**
 * Modelo de domínio que representa as estatísticas diárias de produtividade do utilizador.
 *
 * Esta classe agrega métricas importantes de produtividade para um dia específico,
 * que permite a análise de progresso e desempenho ao longo do tempo.
 */
data class DailyStat(
    // O ID é gerado pelo servidor Strapi
    val id: Int,
    val date: LocalDate,
    val tasksCompleted: Int,
    val tasksCreated: Int,
    val totalPomodoroSessions: Int,
    val totalWorkMinutes: Int,
    val totalBreakMinutes: Int
)

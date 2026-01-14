package pt.ipt.dam2025.nocrastination.domain.models

/**
 * Modelo de domínio que representa o perfil de um utilizador.
 *
 * Contém informações pessoais e preferências de configuração
 * para a aplicação de gestão de produtividade.
 */
data class UserProfile(
    // O ID é gerado pelo servidor Strapi
    val userId: Int,
    val fullName: String,
    val bio: String?,
    val avatarUrl: String?,
    val timezone: String,
    val dailyGoalMinutes: Int,
    val pomodoroWorkDuration: Int,
    val pomodoroShortBreak: Int,
    val pomodoroLongBreak: Int,
    val userEmail: String = ""
)


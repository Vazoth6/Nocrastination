package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

interface UserProfileRepository {
    /**
     * Obtém o perfil do utilizador atual
     */
    suspend fun getMyProfile(): Result<UserProfile>

    /**
     * Atualiza o perfil do utilizador
     * @param id ID do perfil a atualizar
     * @param profile Dados atualizados do perfil
     */
    suspend fun updateProfile(id: Int, profile: UserProfile): Result<UserProfile>
}
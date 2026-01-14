package pt.ipt.dam2025.nocrastination.data.repositories

import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.UserProfileApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdateUserProfileRequest
import pt.ipt.dam2025.nocrastination.data.mapper.UserProfileMapper
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.domain.repository.UserProfileRepository

class UserProfileRepositoryImpl(
    private val userProfileApi: UserProfileApi, // API para operações de perfil de utilizador
    private val userProfileMapper: UserProfileMapper // Mapper para conversão entre DTOs e modelos
) : UserProfileRepository {

    /**
     * Obtém o perfil do utilizador atual
     * @return Result com UserProfile ou erro
     */
    override suspend fun getMyProfile(): Result<UserProfile> {
        return try {
            val response = userProfileApi.getMyProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(userProfileMapper.toDomain(body))
                } else {
                    Result.failure(Exception("Corpo de resposta para o Perfil é nulo"))
                }
            } else {
                Result.failure(Exception("Falha ao carregar perfil: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atualiza o perfil do utilizador
     * @param id ID do perfil a atualizar
     * @param profile Objeto UserProfile com dados atualizados
     * @return Result com UserProfile atualizado ou erro
     */
    override suspend fun updateProfile(id: Int, profile: UserProfile): Result<UserProfile> {
        return try {
            // Cria o pedido específico para atualização de perfil
            val request = UpdateUserProfileRequest(
                data = UpdateUserProfileRequest.ProfileData(
                    fullName = profile.fullName,
                    bio = profile.bio,
                    timezone = profile.timezone,
                    dailyGoalMinutes = profile.dailyGoalMinutes,
                    pomodoroWorkDuration = profile.pomodoroWorkDuration,
                    pomodoroShortBreak = profile.pomodoroShortBreak,
                    pomodoroLongBreak = profile.pomodoroLongBreak
                )
            )

            val response = userProfileApi.updateProfile(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(userProfileMapper.toDomain(body))
                } else {
                    Result.failure(Exception("Update response body is null"))
                }
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
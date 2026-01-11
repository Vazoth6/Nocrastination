package pt.ipt.dam2025.nocrastination.data.repositories

import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.UserProfileApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdateUserProfileRequest
import pt.ipt.dam2025.nocrastination.data.mapper.UserProfileMapper
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.domain.repository.UserProfileRepository

class UserProfileRepositoryImpl(
    private val userProfileApi: UserProfileApi,
    private val userProfileMapper: UserProfileMapper
) : UserProfileRepository {

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

    override suspend fun updateProfile(id: Int, profile: UserProfile): Result<UserProfile> {
        return try {
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
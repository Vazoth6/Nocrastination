package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.UserProfile

interface UserProfileRepository {
    suspend fun getMyProfile(): Result<UserProfile>
    suspend fun updateProfile(id: Int, profile: UserProfile): Result<UserProfile>
}
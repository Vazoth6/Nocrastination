package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdateUserProfileRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserProfileResponse
import retrofit2.Response
import retrofit2.http.*

interface UserProfileApi {
    @GET("api/user-profiles/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @PUT("api/user-profiles/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileResponse>
}
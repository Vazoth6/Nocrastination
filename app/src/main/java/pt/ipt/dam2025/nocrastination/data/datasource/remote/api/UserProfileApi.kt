package pt.ipt.dam2025.nocrastination.data.datasource.remote.api

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.UpdateUserProfileRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Path

interface UserProfileApi {
    /*
        Obtém o perfil do utilizador autenticado
        Usa "/me" em vez de ID, o que é comum para endpoints de "utilizador atual"
    */
    @GET("api/user-profile/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    /*
        Atualiza um perfil de utilizador
        O UPDATE requer ID explícito
    */
    @PUT("api/user-profile/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body profileData: UpdateUserProfileRequest
    ): Response<UserProfileResponse>
}
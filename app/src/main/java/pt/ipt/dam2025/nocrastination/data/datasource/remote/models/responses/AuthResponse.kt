package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val jwt: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val confirmed: Boolean
)
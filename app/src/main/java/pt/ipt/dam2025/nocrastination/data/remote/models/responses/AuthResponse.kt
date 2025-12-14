package pt.ipt.dam2025.nocrastination.data.remote.models.responses

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("jwt")
    val jwt: String,

    @SerializedName("user")
    val user: UserResponse
)

data class UserResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("confirmed")
    val confirmed: Boolean,

    @SerializedName("blocked")
    val blocked: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
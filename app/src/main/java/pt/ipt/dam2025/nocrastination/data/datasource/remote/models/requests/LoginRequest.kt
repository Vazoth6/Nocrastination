package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("identifier") // Strapi uses "identifier" for email/username
    val identifier: String,

    @SerializedName("password")
    val password: String
)
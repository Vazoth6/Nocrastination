package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

import com.google.gson.annotations.SerializedName

data class FocusLocationRequest(
    @SerializedName("data")
    val data: FocusLocationRequestData
)

data class FocusLocationRequestData(
    @SerializedName("attributes")
    val attributes: FocusLocationAttributes
)

data class FocusLocationAttributes(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("radius")
    val radius: Float,

    @SerializedName("enabled")
    val enabled: Boolean = true,

    @SerializedName("notificationMessage")
    val notificationMessage: String = "Vamos pôr as mãos ao trabalho!"
)

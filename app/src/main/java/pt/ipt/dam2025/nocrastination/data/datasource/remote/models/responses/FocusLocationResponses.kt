package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses

import com.google.gson.annotations.SerializedName

data class FocusLocationResponse(
    @SerializedName("data")
    val data: FocusLocationData
)

data class FocusLocationListResponse(
    @SerializedName("data")
    val data: List<FocusLocationData>
)

data class FocusLocationData(
    @SerializedName("id")
    val id: Int,
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
    val enabled: Boolean,

    @SerializedName("notificationMessage")
    val notificationMessage: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
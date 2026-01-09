package pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests

data class CreateFocusLocationRequest(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f,
    val enabled: Boolean = true,
    val notificationMessage: String = "Vamos pôr as mãos ao trabalho!"
)

data class UpdateFocusLocationRequest(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float? = null,
    val enabled: Boolean? = null,
    val notificationMessage: String? = null
)

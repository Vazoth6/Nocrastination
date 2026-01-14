package pt.ipt.dam2025.nocrastination.data.mapper

import android.util.Log
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.FocusLocationAttributes
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.FocusLocationRequest
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.FocusLocationRequestData
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationData
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

class FocusLocationMapper {

    // Converte de DTO (Data Transfer Object) da API para modelo de domínio
    fun mapToDomain(data: FocusLocationData): FocusLocation {
        return FocusLocation(
            id = data.id,
            name = data.name,
            address = data.address,
            latitude = data.latitude,
            longitude = data.longitude,
            radius = data.radius,
            enabled = data.enabled,
            notificationMessage = data.notificationMessage,
            createdAt = data.createdAt,
            updatedAt = data.updatedAt
        )
    }

    // Converte de modelo de domínio para request de criação da API
    fun mapToCreateRequest(location: FocusLocation): FocusLocationRequest {
        Log.d("FocusLocationMapper", "A mapear para CreateRequest: ${location.name}")

        return FocusLocationRequest(
            data = FocusLocationRequestData(
                attributes = FocusLocationAttributes(
                    name = location.name,
                    address = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = location.radius,
                    enabled = location.enabled,
                    notificationMessage = location.notificationMessage
                )
            )
        )
    }

    // Converte de modelo de domínio para request de atualização da API
    fun mapToUpdateRequest(location: FocusLocation): FocusLocationRequest {
        Log.d("FocusLocationMapper", "A mapear para UpdateRequest: ID=${location.id}")

        return FocusLocationRequest(
            data = FocusLocationRequestData(
                attributes = FocusLocationAttributes(
                    name = location.name,
                    address = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = location.radius,
                    enabled = location.enabled,
                    notificationMessage = location.notificationMessage
                )
            )
        )
    }
}
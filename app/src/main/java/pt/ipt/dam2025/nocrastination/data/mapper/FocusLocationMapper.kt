package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.*
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationData
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

class FocusLocationMapper {
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

    fun mapToCreateRequest(location: FocusLocation): CreateFocusLocationRequest {
        return CreateFocusLocationRequest(
            name = location.name,
            address = location.address,
            latitude = location.latitude,
            longitude = location.longitude,
            radius = location.radius,
            enabled = location.enabled,
            notificationMessage = location.notificationMessage
        )
    }

    fun mapToUpdateRequest(location: FocusLocation): UpdateFocusLocationRequest {
        return UpdateFocusLocationRequest(
            name = location.name,
            address = location.address,
            latitude = location.latitude,
            longitude = location.longitude,
            radius = location.radius,
            enabled = location.enabled,
            notificationMessage = location.notificationMessage
        )
    }
}
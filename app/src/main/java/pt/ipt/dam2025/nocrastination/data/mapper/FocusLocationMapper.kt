package pt.ipt.dam2025.nocrastination.data.mapper

import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.*
import pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.FocusLocationData
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

class FocusLocationMapper {
    fun mapToDomain(data: FocusLocationData): FocusLocation {
        return FocusLocation(
            id = data.id,
            name = data.attributes.name,
            address = data.attributes.address,
            latitude = data.attributes.latitude,
            longitude = data.attributes.longitude,
            radius = data.attributes.radius,
            enabled = data.attributes.enabled,
            notificationMessage = data.attributes.notificationMessage,
            createdAt = data.attributes.createdAt,
            updatedAt = data.attributes.updatedAt
        )
    }

    fun mapToCreateRequest(location: FocusLocation): CreateFocusLocationRequest {
        return CreateFocusLocationRequest(
            data = CreateFocusLocationRequest.Data(
                attributes = CreateFocusLocationRequest.Attributes(
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

    fun mapToUpdateRequest(location: FocusLocation): UpdateFocusLocationRequest {
        return UpdateFocusLocationRequest(
            data = UpdateFocusLocationRequest.Data(
                attributes = UpdateFocusLocationRequest.Attributes(
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
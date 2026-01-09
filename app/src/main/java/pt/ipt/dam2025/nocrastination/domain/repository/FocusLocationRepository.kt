package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.Result

interface FocusLocationRepository {
    suspend fun getFocusLocations(): Result<List<FocusLocation>>
    suspend fun getFocusLocationById(id: Int): Result<FocusLocation>
    suspend fun createFocusLocation(location: FocusLocation): Result<FocusLocation>
    suspend fun updateFocusLocation(location: FocusLocation): Result<FocusLocation>
    suspend fun deleteFocusLocation(id: Int): Result<Unit>
    suspend fun toggleFocusLocation(id: Int, enabled: Boolean): Result<FocusLocation>
}
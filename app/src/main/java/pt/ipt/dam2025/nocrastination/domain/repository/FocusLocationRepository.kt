package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.Result

interface FocusLocationRepository {
    /**
     * Obtém todos os locais de foco do utilizador
     */
    suspend fun getFocusLocations(): Result<List<FocusLocation>>

    /**
     * Obtém um local de foco específico pelo ID
     */
    suspend fun getFocusLocationById(id: Int): Result<FocusLocation>

    /**
     * Cria um novo local de foco
     */
    suspend fun createFocusLocation(location: FocusLocation): Result<FocusLocation>

    /**
     * Atualiza um local de foco existente
     */
    suspend fun updateFocusLocation(location: FocusLocation): Result<FocusLocation>

    /**
     * Elimina um local de foco pelo ID
     */
    suspend fun deleteFocusLocation(id: Int): Result<Unit>

    /**
     * Ativa/desativa um local de foco (toggle)
     * @param enabled true para ativar, false para desativar
     */
    suspend fun toggleFocusLocation(id: Int, enabled: Boolean): Result<FocusLocation>
}
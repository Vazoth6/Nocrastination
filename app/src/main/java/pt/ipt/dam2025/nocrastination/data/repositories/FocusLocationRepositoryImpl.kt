package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.FocusLocationApi
import pt.ipt.dam2025.nocrastination.data.mapper.FocusLocationMapper
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.repository.FocusLocationRepository

class FocusLocationRepositoryImpl(
    private val focusLocationApi: FocusLocationApi,
    private val focusLocationMapper: FocusLocationMapper
) : FocusLocationRepository {

    override suspend fun getFocusLocations(): Result<List<FocusLocation>> {
        Log.d("FocusLocationRepo", "🔄 Buscando focus locations da API...")

        return try {
            val response = focusLocationApi.getFocusLocations()

            Log.d("FocusLocationRepo", "📡 Resposta código: ${response.code()}")
            Log.d("FocusLocationRepo", "📡 Resposta mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", "✅ ${apiResponse.data.size} focus locations recebidas")
                    val locations = apiResponse.data.map { focusLocationMapper.mapToDomain(it) }
                    Result.Success(locations)
                } ?: run {
                    Log.w("FocusLocationRepo", "⚠️ Resposta vazia")
                    Result.Success(emptyList())
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", "❌ Erro na resposta: ${response.code()} - $errorBody")

                when (response.code()) {
                    401 -> Result.Error(Exception("Não autenticado. Faça login novamente."))
                    403 -> Result.Error(Exception("Não tem permissão para aceder a este recurso"))
                    else -> Result.Error(Exception("Falha ao buscar localizações: ${response.code()} $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", "❌ Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun getFocusLocationById(id: Int): Result<FocusLocation> {
        return try {
            val response = focusLocationApi.getFocusLocationById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val location = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(location)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Focus location not found: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createFocusLocation(location: FocusLocation): Result<FocusLocation> {
        return try {
            val request = focusLocationMapper.mapToCreateRequest(location)
            val response = focusLocationApi.createFocusLocation(request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val createdLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(createdLocation)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to create focus location: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateFocusLocation(location: FocusLocation): Result<FocusLocation> {
        return try {
            val request = focusLocationMapper.mapToUpdateRequest(location)
            val response = focusLocationApi.updateFocusLocation(location.id!!, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val updatedLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedLocation)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to update focus location: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteFocusLocation(id: Int): Result<Unit> {
        return try {
            val response = focusLocationApi.deleteFocusLocation(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to delete focus location: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleFocusLocation(id: Int, enabled: Boolean): Result<FocusLocation> {
        return try {
            val request = mapOf("enabled" to enabled)
            val response = focusLocationApi.toggleFocusLocation(id, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val location = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(location)
                } ?: Result.Error(Exception("Empty response"))
            } else {
                Result.Error(Exception("Failed to toggle focus location: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
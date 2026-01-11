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
        Log.d("FocusLocationRepo", "🔄 Criando focus location: ${location.name}")
        Log.d("FocusLocationRepo", "📍 Dados: lat=${location.latitude}, lon=${location.longitude}, radius=${location.radius}")

        return try {
            val request = focusLocationMapper.mapToCreateRequest(location)

            // Log detalhado do request
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            val jsonRequest = gson.toJson(request)
            Log.d("FocusLocationRepo", "📤 JSON sendo enviado:\n$jsonRequest")

            // Log dos headers (se houver autenticação)
            Log.d("FocusLocationRepo", "🔑 Verificando autenticação...")

            val response = focusLocationApi.createFocusLocation(request)
            Log.d("FocusLocationRepo", "📥 Response code: ${response.code()}")
            Log.d("FocusLocationRepo", "📥 Response message: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", "✅ Resposta da API: ${apiResponse.data}")
                    val createdLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Log.d("FocusLocationRepo", "✅ Mapeado para domínio: ID=${createdLocation.id}")
                    Result.Success(createdLocation)
                } ?: run {
                    Log.e("FocusLocationRepo", "❌ Response body é null")
                    Result.Error(Exception("Resposta vazia da API"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", "❌ Erro na API: ${response.code()} - $errorBody")

                // Tentar entender melhor o erro
                if (response.code() == 500) {
                    Log.e("FocusLocationRepo", "🔍 Erro 500 - Problema interno no servidor")
                    Log.e("FocusLocationRepo", "🔍 Possíveis causas:")
                    Log.e("FocusLocationRepo", "   1. Campo 'type' faltando ou incorreto")
                    Log.e("FocusLocationRepo", "   2. Validação de dados falhou (ex: latitude inválida)")
                    Log.e("FocusLocationRepo", "   3. Erro no banco de dados do servidor")
                    Log.e("FocusLocationRepo", "   4. Autenticação/authorização problemática")
                }

                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", "❌ Exceção na criação: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun updateFocusLocation(location: FocusLocation): Result<FocusLocation> {
        Log.d("FocusLocationRepo", "🔄 Atualizando focus location ID: ${location.id}")

        return try {
            // USANDO FORMATO WRAPPER (JSON:API)
            val request = focusLocationMapper.mapToUpdateRequest(location)

            // Log do request como JSON
            val gson = com.google.gson.Gson()
            val jsonRequest = gson.toJson(request)
            Log.d("FocusLocationRepo", "📤 JSON sendo enviado (wrapper): $jsonRequest")

            val response = focusLocationApi.updateFocusLocation(location.id!!, request)
            Log.d("FocusLocationRepo", "📥 Response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", "✅ Focus location atualizada")
                    val updatedLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedLocation)
                } ?: run {
                    Log.e("FocusLocationRepo", "❌ Response body é null")
                    Result.Error(Exception("Resposta vazia da API"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", "❌ Erro na API: ${response.code()} - $errorBody")
                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", "❌ Exceção na atualização: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun deleteFocusLocation(id: Int): Result<Unit> {
        Log.d("FocusLocationRepo", "🔄 Apagando focus location ID: $id")

        return try {
            val response = focusLocationApi.deleteFocusLocation(id)
            Log.d("FocusLocationRepo", "📥 Response code: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("FocusLocationRepo", "✅ Focus location apagada")
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", "❌ Erro na API: ${response.code()} - $errorBody")
                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", "❌ Exceção na eliminação: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun toggleFocusLocation(id: Int, enabled: Boolean): Result<FocusLocation> {
        Log.d("FocusLocationRepo", "🔄 Alternando focus location ID: $id para $enabled")

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
package pt.ipt.dam2025.nocrastination.data.repositories

import android.util.Log
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.FocusLocationApi
import pt.ipt.dam2025.nocrastination.data.mapper.FocusLocationMapper
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.repository.FocusLocationRepository

class FocusLocationRepositoryImpl(
    private val focusLocationApi: FocusLocationApi, // API para operações de locais de foco
    private val focusLocationMapper: FocusLocationMapper // Mapper para conversão entre DTOs e modelos de domínio
) : FocusLocationRepository {

    /**
     * Obtém todos os locais de foco do utilizador
     * @return Result com lista de FocusLocation ou erro
     */
    override suspend fun getFocusLocations(): Result<List<FocusLocation>> {
        Log.d("FocusLocationRepo", " A efetuar busca de locais de foco da API...")

        return try {
            val response = focusLocationApi.getFocusLocations()

            Log.d("FocusLocationRepo", " Resposta código: ${response.code()}")
            Log.d("FocusLocationRepo", " Resposta mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", " ${apiResponse.data.size} locais de foco recebidos")
                    // Converte cada DTO da API para modelo de domínio
                    val locations = apiResponse.data.map { focusLocationMapper.mapToDomain(it) }
                    Result.Success(locations)
                } ?: run {
                    Log.w("FocusLocationRepo", " Resposta vazia")
                    Result.Success(emptyList()) // Retorna lista vazia em vez de erro
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", " Erro na resposta: ${response.code()} - $errorBody")

                // Tratamento específico por código de erro
                when (response.code()) {
                    401 -> Result.Error(Exception("Não autenticado. Faça login novamente."))
                    403 -> Result.Error(Exception("Não tem permissão para aceder a este recurso"))
                    else -> Result.Error(Exception("Falha ao buscar localizações: ${response.code()} $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", " Exceção: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Obtém um local de foco específico pelo ID
     * @param id ID do local de foco
     * @return Result com FocusLocation ou erro
     */
    override suspend fun getFocusLocationById(id: Int): Result<FocusLocation> {
        return try {
            val response = focusLocationApi.getFocusLocationById(id)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val location = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(location)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                Result.Error(Exception("Local de foco não encontrado: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Cria um novo local de foco
     * @param location Objeto FocusLocation com dados para criação
     * @return Result com o local de foco criado ou erro
     */
    override suspend fun createFocusLocation(location: FocusLocation): Result<FocusLocation> {
        Log.d("FocusLocationRepo", " A criar local de foco: ${location.name}")
        Log.d("FocusLocationRepo", " Dados: lat=${location.latitude}, lon=${location.longitude}, radius=${location.radius}")

        return try {
            // Converte o modelo de domínio para o DTO de criação
            val request = focusLocationMapper.mapToCreateRequest(location)

            // Log detalhado do request em formato JSON para debugging
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            val jsonRequest = gson.toJson(request)
            Log.d("FocusLocationRepo", " JSON a ser enviado:\n$jsonRequest")

            // Log dos headers (importante para verificar autenticação)
            Log.d("FocusLocationRepo", " Verificando autenticação...")

            val response = focusLocationApi.createFocusLocation(request)
            Log.d("FocusLocationRepo", " Resposta código: ${response.code()}")
            Log.d("FocusLocationRepo", " Resposta mensagem: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", " Resposta da API: ${apiResponse.data}")
                    val createdLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Log.d("FocusLocationRepo", " Mapeado para domínio: ID=${createdLocation.id}")
                    Result.Success(createdLocation)
                } ?: run {
                    Log.e("FocusLocationRepo", " Corpo de resposta é null")
                    Result.Error(Exception("Resposta vazia da API"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", " Erro na API: ${response.code()} - $errorBody")

                // Análise detalhada para erros 500 (interno do servidor)
                if (response.code() == 500) {
                    Log.e("FocusLocationRepo", " Erro 500 - Problema interno no servidor")
                    Log.e("FocusLocationRepo", " Possíveis causas:")
                    Log.e("FocusLocationRepo", "   1. Campo 'type' faltando ou incorreto")
                    Log.e("FocusLocationRepo", "   2. Validação de dados falhou (ex: latitude inválida)")
                    Log.e("FocusLocationRepo", "   3. Erro na base de dados do servidor")
                    Log.e("FocusLocationRepo", "   4. Autenticação/autorização problemática")
                }

                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", " Exceção na criação: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Atualiza um local de foco existente
     * @param location Objeto FocusLocation com dados atualizados
     * @return Result com o local de foco atualizado ou erro
     */
    override suspend fun updateFocusLocation(location: FocusLocation): Result<FocusLocation> {
        Log.d("FocusLocationRepo", " A atualizar o ID do local de foco: ${location.id}")

        return try {
            // Formato wrapper JSON:API (padrão usado pela API)
            val request = focusLocationMapper.mapToUpdateRequest(location)

            // Log do request como JSON para debugging
            val gson = com.google.gson.Gson()
            val jsonRequest = gson.toJson(request)
            Log.d("FocusLocationRepo", " JSON a ser enviado (wrapper): $jsonRequest")

            val response = focusLocationApi.updateFocusLocation(location.id!!, request)
            Log.d("FocusLocationRepo", " Resposta código: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("FocusLocationRepo", " Local de foco atualizado")
                    val updatedLocation = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(updatedLocation)
                } ?: run {
                    Log.e("FocusLocationRepo", " Corpo de resposta é null")
                    Result.Error(Exception("Resposta vazia da API"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", " Erro na API: ${response.code()} - $errorBody")
                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", " Exceção na atualização: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Elimina um local de foco pelo ID
     * @param id ID do local de foco a eliminar
     * @return Result com Unit em caso de sucesso ou erro
     */
    override suspend fun deleteFocusLocation(id: Int): Result<Unit> {
        Log.d("FocusLocationRepo", " A apagar local de foco ID: $id")

        return try {
            val response = focusLocationApi.deleteFocusLocation(id)
            Log.d("FocusLocationRepo", " Resposta código: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("FocusLocationRepo", " Local de foco apagado")
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                Log.e("FocusLocationRepo", " Erro na API: ${response.code()} - $errorBody")
                Result.Error(Exception("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FocusLocationRepo", " Exceção na eliminação: ${e.message}", e)
            Result.Error(e)
        }
    }

    /**
     * Ativa/desativa um local de foco (toggle)
     * @param id ID do local de foco
     * @param enabled Estado desejado (true = ativo, false = inativo)
     * @return Result com o local de foco atualizado ou erro
     */
    override suspend fun toggleFocusLocation(id: Int, enabled: Boolean): Result<FocusLocation> {
        Log.d("FocusLocationRepo", " Alternando focus location ID: $id para $enabled")

        return try {
            // Cria um map simples para o corpo do request
            val request = mapOf("enabled" to enabled)
            val response = focusLocationApi.toggleFocusLocation(id, request)

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val location = focusLocationMapper.mapToDomain(apiResponse.data)
                    Result.Success(location)
                } ?: Result.Error(Exception("Resposta vazia"))
            } else {
                Result.Error(Exception("Falha ao desligar local de foco: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
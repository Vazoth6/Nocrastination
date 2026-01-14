package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation
import pt.ipt.dam2025.nocrastination.domain.models.Result
import pt.ipt.dam2025.nocrastination.domain.models.UIEvent
import pt.ipt.dam2025.nocrastination.domain.repository.FocusLocationRepository
import pt.ipt.dam2025.nocrastination.utils.GeofencingManager

class FocusLocationViewModel(
    private val focusLocationRepository: FocusLocationRepository, // Repositório de dados
    private val geofencingManager: GeofencingManager // Gestor de geofences do Android
) : ViewModel() {

    // Estados observáveis usando StateFlow (alternativa moderna ao LiveData)
    private val _focusLocations = MutableStateFlow<List<FocusLocation>>(emptyList())
    val focusLocations: StateFlow<List<FocusLocation>> = _focusLocations.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Eventos UI únicos (SharedFlow com replay=0)
    private val _uiEvents = MutableSharedFlow<UIEvent>()
    val uiEvents = _uiEvents

    /**
     * Carrega todas as localizações de foco do utilizador
     * Atualiza os geofences correspondentes no sistema Android
     */
    fun loadFocusLocations() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.getFocusLocations()) {
                is Result.Success -> {
                    _focusLocations.value = result.data
                    // Atualizar geofences no sistema Android
                    geofencingManager.addGeofencesForFocusLocations(result.data)
                    _uiEvents.emit(UIEvent.ShowToast("${result.data.size} zonas de foco carregadas"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao carregar localizações"
                }
            }
            _loading.value = false
        }
    }

    /**
     * Cria uma nova localização de foco
     * @param location Dados da localização a criar
     */
    fun createFocusLocation(location: FocusLocation) {
        Log.d("FocusLocationVM", " Iniciando createFocusLocation: ${location.name}")
        Log.d("FocusLocationVM", " Dados da localização: lat=${location.latitude}, lon=${location.longitude}")

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            Log.d("FocusLocationVM", "📤 Chamando repositório...")

            when (val result = focusLocationRepository.createFocusLocation(location)) {
                is Result.Success -> {
                    Log.d("FocusLocationVM", " Sucesso! Localização criada: ${result.data.id}")
                    Log.d("FocusLocationVM", " Dados retornados: ${result.data}")

                    // Adiciona à lista existente (imutabilidade)
                    _focusLocations.value = _focusLocations.value + result.data

                    Log.d("FocusLocationVM", " Total de localizações: ${_focusLocations.value.size}")

                    // Tentar recarregar geofences
                    try {
                        geofencingManager.addGeofencesForFocusLocations(_focusLocations.value)
                        Log.d("FocusLocationVM", " Geofences atualizados")
                    } catch (e: Exception) {
                        Log.e("FocusLocationVM", " Erro em geofences: ${e.message}")
                    }

                    _uiEvents.emit(UIEvent.ShowToast("Zona de foco criada!"))
                }
                is Result.Error -> {
                    Log.e("FocusLocationVM", "❌ Erro no repositório: ${result.exception.message}", result.exception)
                    _error.value = result.exception.message ?: "Erro ao criar localização"
                    _uiEvents.emit(UIEvent.ShowToast("Erro: ${result.exception.message}"))
                }
            }
            _loading.value = false
        }
    }

    /**
     * Atualiza uma localização de foco existente
     * @param location Dados atualizados da localização
     */
    fun updateFocusLocation(location: FocusLocation) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.updateFocusLocation(location)) {
                is Result.Success -> {
                    // Atualiza na lista mantendo imutabilidade
                    _focusLocations.value = _focusLocations.value.map {
                        if (it.id == result.data.id) result.data else it
                    }
                    // Recarregar todos os geofences
                    geofencingManager.addGeofencesForFocusLocations(_focusLocations.value)
                    _uiEvents.emit(UIEvent.ShowToast("Zona de foco atualizada!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao atualizar localização"
                }
            }
            _loading.value = false
        }
    }

    /**
     * Elimina uma localização de foco
     * @param id ID da localização a eliminar
     */
    fun deleteFocusLocation(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.deleteFocusLocation(id)) {
                is Result.Success -> {
                    // Remove da lista
                    _focusLocations.value = _focusLocations.value.filter { it.id != id }
                    // Remover geofence específico
                    geofencingManager.removeGeofence(id)
                    _uiEvents.emit(UIEvent.ShowToast("Zona de foco eliminada!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao eliminar localização"
                }
            }
            _loading.value = false
        }
    }

    /**
     * Ativa/desativa uma localização de foco
     * @param id ID da localização
     * @param enabled true para ativar, false para desativar
     */
    fun toggleFocusLocation(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.toggleFocusLocation(id, enabled)) {
                is Result.Success -> {
                    _focusLocations.value = _focusLocations.value.map {
                        if (it.id == id) result.data else it
                    }
                    // Recarregar todos os geofences
                    geofencingManager.addGeofencesForFocusLocations(_focusLocations.value)
                    val status = if (enabled) "ativada" else "desativada"
                    _uiEvents.emit(UIEvent.ShowToast("Zona $status"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao alterar estado"
                }
            }
            _loading.value = false
        }
    }

    /**
     * Limpa mensagens de erro
     */
    fun clearError() {
        _error.value = null
    }
}
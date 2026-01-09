package pt.ipt.dam2025.nocrastination.presentations.viewmodel

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
    private val focusLocationRepository: FocusLocationRepository,
    private val geofencingManager: GeofencingManager
) : ViewModel() {

    private val _focusLocations = MutableStateFlow<List<FocusLocation>>(emptyList())
    val focusLocations: StateFlow<List<FocusLocation>> = _focusLocations.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Eventos UI
    private val _uiEvents = MutableSharedFlow<UIEvent>()
    val uiEvents = _uiEvents

    fun loadFocusLocations() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.getFocusLocations()) {
                is Result.Success -> {
                    _focusLocations.value = result.data
                    // Atualizar geofences
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

    fun createFocusLocation(location: FocusLocation) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.createFocusLocation(location)) {
                is Result.Success -> {
                    _focusLocations.value = _focusLocations.value + result.data
                    // Recarregar geofences
                    geofencingManager.addGeofencesForFocusLocations(_focusLocations.value)
                    _uiEvents.emit(UIEvent.ShowToast("Zona de foco criada!"))
                }
                is Result.Error -> {
                    _error.value = result.exception.message ?: "Erro ao criar localização"
                }
            }
            _loading.value = false
        }
    }

    fun updateFocusLocation(location: FocusLocation) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.updateFocusLocation(location)) {
                is Result.Success -> {
                    _focusLocations.value = _focusLocations.value.map {
                        if (it.id == result.data.id) result.data else it
                    }
                    // Recarregar geofences
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

    fun deleteFocusLocation(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = focusLocationRepository.deleteFocusLocation(id)) {
                is Result.Success -> {
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

    fun clearError() {
        _error.value = null
    }
}
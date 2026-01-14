package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.models.UserProfile
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.domain.repository.UserProfileRepository
import pt.ipt.dam2025.nocrastination.utils.Resource

class UserProfileViewModel(
    private val authRepository: AuthRepository, // Para obter perfil atual
    private val userProfileRepository: UserProfileRepository // Para atualizar perfil
) : ViewModel() {

    // Estado do perfil do utilizador
    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profileState: StateFlow<UserProfile?> = _profileState.asStateFlow()

    // Estados de loading e erro
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Inicializa o carregamento do perfil automaticamente
    init {
        loadProfile()
    }

    /**
     * Carrega o perfil do utilizador atual
     * Usa AuthRepository pois tem metodo getCurrentUser()
     */
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.getCurrentUser()

            when (result) {
                is Resource.Success -> {
                    _profileState.value = result.data
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }

                is Resource.Loading<*> -> TODO()
            }

            _isLoading.value = false
        }
    }

    /**
     * Atualiza o perfil do utilizador
     * @param profile Dados atualizados do perfil
     */
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Usa userProfileRepository para atualização
            val result = userProfileRepository.updateProfile(profile.userId, profile)

            if (result.isSuccess) {
                _profileState.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update profile"
            }

            _isLoading.value = false
        }
    }

    /**
     * Limpa mensagens de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
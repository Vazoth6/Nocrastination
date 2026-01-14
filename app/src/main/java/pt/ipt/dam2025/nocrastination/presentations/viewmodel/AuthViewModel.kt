package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.utils.Resource

class AuthViewModel(
    private val authRepository: AuthRepository // Repositório de dados
) : ViewModel() {

    // Estados observáveis da UI usando LiveData
    private val _loginState = MutableLiveData<Resource<Unit>?>()
    val loginState: LiveData<Resource<Unit>?> = _loginState


    private val _registerState = MutableLiveData<Resource<Unit>?>()
    val registerState: LiveData<Resource<Unit>?> = _registerState

    /**
     * Efetua o processo de login do utilizador
     * @param email Email do utilizador (ou identificador)
     * @param password Palavra-passe do utilizador
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Define estado de carregamento antes de iniciar operação
            _loginState.value = Resource.Loading()

            // Chama o repositório para efetuar login
            val result = authRepository.login(email, password)

            // Atualiza o estado com o resultado (sucesso ou erro)
            _loginState.value = result
        }
    }

    /**
     * Regista um novo utilizador no sistema
     * @param username Nome de utilizador único
     * @param email Email válido do utilizador
     * @param password Palavra-passe segura
     */
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            // Define estado de carregamento
            _registerState.value = Resource.Loading()

            // Chama o repositório para efetuar registo
            val result = authRepository.register(username, email, password)

            // Atualiza o estado com o resultado
            _registerState.value = result
        }
    }

    /**
     * Limpa o estado de login
     * Útil após navegação ou timeout para evitar reutilização de estados antigos
     */
    fun clearLoginState() {
        _loginState.value = null
    }

    /**
     * Limpa o estado de registo
     */
    fun clearRegisterState() {
        _registerState.value = null
    }

    /**
     * Verifica se existe um utilizador autenticado
     * @return Boolean true se o utilizador estiver logado
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    /**
     * Efetua logout do utilizador atual
     * Remove dados de autenticação e limpa estados locais
     */
    fun logout() {
        // Chama repositório para remover token e dados de sessão
        authRepository.logout()

        // Limpa os estados do ViewModel para garantir consistência
        _loginState.value = null
        _registerState.value = null
    }
}
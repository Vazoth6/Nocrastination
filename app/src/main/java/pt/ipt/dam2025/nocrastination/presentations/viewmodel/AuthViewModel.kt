// AuthViewModel.kt
package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.utils.Resource

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<Unit>?>()
    val loginState: LiveData<Resource<Unit>?> = _loginState

    private val _registerState = MutableLiveData<Resource<Unit>?>()
    val registerState: LiveData<Resource<Unit>?> = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.login(email, password)
            _loginState.value = result
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = authRepository.register(username, email, password)
            _registerState.value = result
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    fun clearRegisterState() {
        _registerState.value = null
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun logout() {
        authRepository.logout()
        // Limpa os estados para garantir
        _loginState.value = null
        _registerState.value = null
    }

}
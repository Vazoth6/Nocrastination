package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepository
import pt.ipt.dam2025.nocrastination.utils.Resource

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<Unit>>()
    val loginState: LiveData<Resource<Unit>> = _loginState

    private val _registerState = MutableLiveData<Resource<Unit>>()
    val registerState: LiveData<Resource<Unit>> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)

                _loginState.value = if (result.isSuccess) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(result.exceptionOrNull()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        _registerState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = authRepository.register(username, email, password)

                _registerState.value = if (result.isSuccess) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(result.exceptionOrNull()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}
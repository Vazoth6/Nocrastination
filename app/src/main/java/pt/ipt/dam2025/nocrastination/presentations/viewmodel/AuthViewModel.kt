package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl
import pt.ipt.dam2025.nocrastination.utils.Resource
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
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
}
package pt.ipt.dam2025.nocrastination.presentations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl

class AuthViewModelFactory(
    private val authRepositoryImpl: AuthRepositoryImpl
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
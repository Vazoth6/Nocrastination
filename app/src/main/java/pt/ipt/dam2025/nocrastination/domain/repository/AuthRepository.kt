// AuthRepository.kt (Interface - domain layer)
package pt.ipt.dam2025.nocrastination.domain.repository

import pt.ipt.dam2025.nocrastination.utils.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun register(username: String, email: String, password: String): Resource<Unit>
    fun logout()
    fun isLoggedIn(): Boolean
}
// User.kt (Domain Model - missing)
package pt.ipt.dam2025.nocrastination.domain.models

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val confirmed: Boolean
)
// domain/model/Result.kt
package pt.ipt.dam2025.nocrastination.domain.models

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrElse(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }
}
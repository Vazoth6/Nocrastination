package pt.ipt.dam2025.nocrastination.utils

// Classe selada para representar o estado de recursos
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    // Dados disponíveis
    class Success<T>(data: T) : Resource<T>(data)
    // Mensagem de erro com dados opcionais
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    // Estado de loading
    class Loading<T> : Resource<T>()
}
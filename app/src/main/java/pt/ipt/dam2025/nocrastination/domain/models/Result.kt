package pt.ipt.dam2025.nocrastination.domain.models

/**
 * Classe selada (sealed class) que representa o resultado de uma operação.
 *
 * Implementa um padrão de "Either" ou "Result" para lidar com operações que podem
 * ter sucesso ou falhar, de forma type-safe e expressiva.
 *
 * @param T Tipo de dados retornado em caso de sucesso
 */
sealed class Result<out T> {
    /**
     * Representa um resultado bem-sucedido.
     *
     * @property data Os dados retornados pela operação
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Representa um resultado com erro.
     *
     * @property exception A exceção que ocorreu durante a operação
     */
    data class Error(val exception: Exception) : Result<Nothing>()

    // Propriedade computada que verifica se o resultado é sucesso
    val isSuccess: Boolean get() = this is Success

    // Propriedade computada que verifica se o resultado é erro
    val isError: Boolean get() = this is Error

    /**
     * Obtém os dados em caso de sucesso, ou null em caso de erro.
     *
     * @return Os dados se for Success, null se for Error
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Obtém os dados em caso de sucesso, ou um valor padrão em caso de erro.
     *
     * @param defaultValue Valor a retornar se o resultado for Error
     * @return Os dados se for Success, defaultValue se for Error
     */
    fun getOrElse(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }
}
package pt.ipt.dam2025.nocrastination.data.exceptions

import retrofit2.HttpException
import java.io.IOException

// Exceção base para erros da API
open class ApiException(
    message: String? = null,
    val code: Int? = null,
    cause: Throwable? = null
) : IOException(message, cause) {
    companion object {
        // Converter HttpException para ApiException
        fun fromHttpException(exception: HttpException): ApiException {
            return ApiException(
                message = exception.message(),
                code = exception.code(),
                cause = exception
            )
        }
    }
}
package pt.ipt.dam2025.nocrastination.data.exceptions

// Exceção para falhas de autenticação
class AuthException(message: String = "Authentication failed") : ApiException(message)
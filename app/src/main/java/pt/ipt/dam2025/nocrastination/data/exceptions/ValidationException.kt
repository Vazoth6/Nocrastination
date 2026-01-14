package pt.ipt.dam2025.nocrastination.data.exceptions

// Exceção para falhas de validação
class ValidationException(message: String = "Validation failed") : IllegalArgumentException(message)
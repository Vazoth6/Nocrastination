package pt.ipt.dam2025.nocrastination.data.exceptions

import java.io.IOException

// Exceção para quando não há ligação à Internet
class NoInternetException(message: String) : IOException(message)
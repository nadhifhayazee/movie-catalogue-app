package com.nadhifhayazee.moviecatalogue.core.util

sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class NoInternetConnection(override val cause: Throwable? = null) :
        NetworkException("No internet connection. Please check your network settings.", cause)

    data class Timeout(override val cause: Throwable? = null) :
        NetworkException("Connection timed out. Please try again.", cause)

    data class ServerError(val code: Int, val serverMessage: String, override val cause: Throwable? = null) :
        NetworkException("Server error: $code - $serverMessage", cause)

    data class Unauthorized(override val cause: Throwable? = null) :
        NetworkException("Authentication failed. Please login again.", cause)

    data class NotFound(override val cause: Throwable? = null) :
        NetworkException("The requested resource was not found.", cause)

    data class Unknown(override val message: String, override val cause: Throwable? = null) :
        NetworkException(message, cause)
}

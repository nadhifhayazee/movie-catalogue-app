package com.nadhifhayazee.moviecatalogue.core.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: NetworkException) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

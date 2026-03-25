package com.nadhifhayazee.moviecatalogue.data.remote

import android.content.Context
import com.nadhifhayazee.moviecatalogue.core.util.Constants
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.NoInternetConnection
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.NotFound
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.ServerError
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Timeout
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Unauthorized
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Unknown
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.data.remote.api.MovieApiService
import com.nadhifhayazee.moviecatalogue.data.remote.dto.toDomain
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val apiService: MovieApiService,
    @ApplicationContext private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

    suspend fun getLatestMovies(page: Int = 1): Result<List<Movie>> {
        android.util.Log.d("RemoteDataSource", "getLatestMovies: Starting page $page")
        return try {
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Calling API")
            val response = apiService.getLatestMovies(Constants.API_KEY, page)
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Got ${response.results.size} movies on page ${response.page}")
            val movies = response.results.map { it.toDomain() }
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Emitted success")
            Result.Success(movies)
        } catch (e: Exception) {
            val error = handleException(e)
            android.util.Log.e("RemoteDataSource", "getLatestMovies: Error", e)
            Result.Error(error)
        }
    }

    suspend fun getTopRatedMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getTopRatedMovies(Constants.API_KEY, page)
            val movies = response.results.map { it.toDomain() }
            Result.Success(movies)
        } catch (e: Exception) {
            val error = handleException(e)
            Result.Error(error)
        }
    }

    suspend fun getRecommendedMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getRecommendedMovies(Constants.API_KEY, page)
            val movies = response.results.map { it.toDomain() }
            Result.Success(movies)
        } catch (e: Exception) {
            val error = handleException(e)
            Result.Error(error)
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<Movie> {
        return try {
            val response = apiService.getMovieDetail(movieId, Constants.API_KEY)
            val movie = response.toDomain()
            Result.Success(movie)
        } catch (e: Exception) {
            val error = handleException(e)
            Result.Error(error)
        }
    }

    suspend fun searchMovies(query: String, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.searchMovies(Constants.API_KEY, query, page)
            val movies = response.results.map { it.toDomain() }
            Result.Success(movies)
        } catch (e: Exception) {
            val error = handleException(e)
            Result.Error(error)
        }
    }

    private fun handleException(exception: Exception): NetworkException {
        return when (exception) {
            is UnknownHostException, is IOException -> {
                checkConnectivity()
            }
            is SocketTimeoutException, is TimeoutException -> {
                Timeout(exception)
            }
            is retrofit2.HttpException -> {
                when (exception.code()) {
                    401 -> Unauthorized(exception)
                    404 -> NotFound(exception)
                    in 400..499 -> ServerError(exception.code(), "Client error", exception)
                    in 500..599 -> ServerError(exception.code(), "Server error", exception)
                    else -> Unknown(exception.message ?: "HTTP error", exception)
                }
            }
            else -> Unknown(exception.message ?: "An unknown error occurred", exception)
        }
    }

    private fun checkConnectivity(): NetworkException {
        return if (!isNetworkAvailable()) {
            NoInternetConnection()
        } else {
            Unknown("Network error occurred", null)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

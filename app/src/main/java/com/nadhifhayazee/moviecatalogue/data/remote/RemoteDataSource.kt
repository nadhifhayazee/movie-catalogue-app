package com.nadhifhayazee.moviecatalogue.data.remote

import com.nadhifhayazee.moviecatalogue.core.util.Constants
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.data.remote.api.MovieApiService
import com.nadhifhayazee.moviecatalogue.data.remote.dto.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: MovieApiService
) {

    fun getLatestMovies(page: Int = 1): Flow<Result<List<com.nadhifhayazee.moviecatalogue.domain.model.Movie>>> = flow {
        android.util.Log.d("RemoteDataSource", "getLatestMovies: Starting page $page")
        emit(Result.Loading)
        try {
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Calling API")
            val response = apiService.getLatestMovies(Constants.API_KEY, page)
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Got ${response.results.size} movies on page ${response.page}")
            val movies = response.results.map { it.toDomain() }
            emit(Result.Success(movies))
            android.util.Log.d("RemoteDataSource", "getLatestMovies: Emitted success")
        } catch (e: Exception) {
            android.util.Log.e("RemoteDataSource", "getLatestMovies: Error", e)
            emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getTopRatedMovies(page: Int = 1): Flow<Result<List<com.nadhifhayazee.moviecatalogue.domain.model.Movie>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getTopRatedMovies(Constants.API_KEY, page)
            val movies = response.results.map { it.toDomain() }
            emit(Result.Success(movies))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getRecommendedMovies(page: Int = 1): Flow<Result<List<com.nadhifhayazee.moviecatalogue.domain.model.Movie>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getRecommendedMovies(Constants.API_KEY, page)
            val movies = response.results.map { it.toDomain() }
            emit(Result.Success(movies))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getMovieDetail(movieId: Int): Flow<Result<com.nadhifhayazee.moviecatalogue.domain.model.Movie>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getMovieDetail(movieId, Constants.API_KEY)
            val movie = response.toDomain()
            emit(Result.Success(movie))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun searchMovies(query: String, page: Int = 1): Flow<Result<List<com.nadhifhayazee.moviecatalogue.domain.model.Movie>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.searchMovies(Constants.API_KEY, query, page)
            val movies = response.results.map { it.toDomain() }
            emit(Result.Success(movies))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }
}

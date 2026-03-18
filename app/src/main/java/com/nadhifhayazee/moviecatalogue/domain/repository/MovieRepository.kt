package com.nadhifhayazee.moviecatalogue.domain.repository

import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getLatestMovies(page: Int = 1): Flow<Result<List<Movie>>>
    suspend fun getTopRatedMovies(page: Int = 1): Flow<Result<List<Movie>>>
    suspend fun getRecommendedMovies(page: Int = 1): Flow<Result<List<Movie>>>
    suspend fun getMovieDetail(movieId: Int): Flow<Result<Movie>>
    suspend fun searchMovies(query: String, page: Int = 1): Flow<Result<List<Movie>>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    suspend fun addFavoriteMovie(movie: Movie)
    suspend fun removeFavoriteMovie(movieId: Int)
    fun isFavoriteMovie(movieId: Int): Flow<Boolean>
}

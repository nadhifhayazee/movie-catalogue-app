package com.nadhifhayazee.moviecatalogue.data.local

import com.nadhifhayazee.moviecatalogue.data.local.dao.MovieDao
import com.nadhifhayazee.moviecatalogue.data.local.entity.FavoriteMovieEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.CachedMovieEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.toDomain
import com.nadhifhayazee.moviecatalogue.data.local.entity.toFavoriteEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.toCachedEntity
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val movieDao: MovieDao
) {
    fun getFavoriteMovies(): Flow<List<FavoriteMovieEntity>> {
        return movieDao.getFavoriteMovies()
    }

    fun isFavoriteMovie(movieId: Int): Flow<Boolean> {
        return movieDao.isFavoriteMovie(movieId)
    }

    suspend fun insertFavorite(movie: FavoriteMovieEntity) {
        movieDao.insertFavorite(movie)
    }

    suspend fun deleteFavorite(movie: FavoriteMovieEntity) {
        movieDao.deleteFavorite(movie)
    }

    suspend fun deleteFavoriteById(movieId: Int) {
        movieDao.deleteFavoriteById(movieId)
    }

    // Cache methods for offline support
    fun getCachedMoviesByCategory(category: String): Flow<List<Movie>> {
        return movieDao.getCachedMoviesByCategory(category).map { entities ->
            entities.map { it.toDomain(isFavorite = false) }
        }
    }

    suspend fun getCachedMoviesByCategoryPaged(
        category: String,
        limit: Int,
        offset: Int
    ): List<Movie> {
        val entities = movieDao.getCachedMoviesByCategoryPaged(category, limit, offset)
        return entities.map { entity ->
            entity.toDomain(isFavorite = false)
        }
    }

    suspend fun cacheMovies(movies: List<Movie>, category: String) {
        val entities = movies.map { movie ->
            movie.toCachedEntity(category)
        }
        movieDao.cacheMovies(entities)
    }

    suspend fun clearCachedMovies(category: String) {
        movieDao.clearCachedMovies(category)
    }

    suspend fun clearOldCache(maxAgeMillis: Long) {
        val timestamp = System.currentTimeMillis() - maxAgeMillis
        movieDao.clearOldCache(timestamp)
    }
}

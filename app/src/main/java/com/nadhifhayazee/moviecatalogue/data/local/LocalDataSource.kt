package com.nadhifhayazee.moviecatalogue.data.local

import com.nadhifhayazee.moviecatalogue.data.local.dao.MovieDao
import com.nadhifhayazee.moviecatalogue.data.local.entity.MovieEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.toDomain
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val movieDao: MovieDao
) {
    fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun isFavoriteMovie(movieId: Int): Flow<Boolean> {
        return movieDao.isFavoriteMovie(movieId)
    }

    suspend fun insertFavorite(movie: MovieEntity) {
        movieDao.insertFavorite(movie)
    }

    suspend fun deleteFavorite(movie: MovieEntity) {
        movieDao.deleteFavorite(movie)
    }

    suspend fun deleteFavoriteById(movieId: Int) {
        movieDao.deleteFavoriteById(movieId)
    }
}

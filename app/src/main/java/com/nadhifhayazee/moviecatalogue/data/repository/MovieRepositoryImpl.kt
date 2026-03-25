package com.nadhifhayazee.moviecatalogue.data.repository

import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.data.local.LocalDataSource
import com.nadhifhayazee.moviecatalogue.data.local.entity.toDomain
import com.nadhifhayazee.moviecatalogue.data.local.entity.toFavoriteEntity
import com.nadhifhayazee.moviecatalogue.data.remote.RemoteDataSource
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : MovieRepository {

    override suspend fun getLatestMovies(page: Int): Flow<Result<List<Movie>>> {
        return getMoviesOfflineFirst(MovieCategory.LATEST.name, page) {
            remoteDataSource.getLatestMovies(page)
        }
    }

    override suspend fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> {
        return getMoviesOfflineFirst(MovieCategory.TOP_RATED.name, page) {
            remoteDataSource.getTopRatedMovies(page)
        }
    }

    override suspend fun getRecommendedMovies(page: Int): Flow<Result<List<Movie>>> {
        return getMoviesOfflineFirst(MovieCategory.RECOMMENDED.name, page) {
            remoteDataSource.getRecommendedMovies(page)
        }
    }

    private fun getMoviesOfflineFirst(
        category: String,
        page: Int,
        remoteCall: suspend () -> Result<List<Movie>>
    ): Flow<Result<List<Movie>>> {
        val favoriteIdsFlow = localDataSource.getFavoriteMovies()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .distinctUntilChanged()

        val dataFlow = flow {
            // First, try to emit cached data immediately (offline-first)
            val cachedMovies = try {
                if (page == 1) {
                    localDataSource.getCachedMoviesByCategory(category).first()
                } else {
                    val offset = (page - 1) * 20
                    localDataSource.getCachedMoviesByCategoryPaged(category, 20, offset)
                }
            } catch (e: Exception) {
                emptyList()
            }

            if (cachedMovies.isNotEmpty()) {
                emit(Result.Success(cachedMovies))
            }

            // Then fetch from remote
            try {
                val result = remoteCall()
                when (result) {
                    is Result.Success -> {
                        // Cache fresh data
                        if (page == 1) {
                            localDataSource.clearCachedMovies(category)
                        }
                        localDataSource.cacheMovies(result.data, category)
                        emit(Result.Success(result.data))
                    }
                    is Result.Error -> {
                        // If we didn't have cached data, emit error
                        if (cachedMovies.isEmpty()) {
                            emit(result)
                        }
                    }
                    is Result.Loading -> emit(Result.Loading)
                }
            } catch (e: Exception) {
                if (cachedMovies.isEmpty()) {
                    emit(Result.Error(com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Unknown(
                        e.message ?: "An unknown error occurred", e
                    )))
                }
            }
        }

        return combine(dataFlow, favoriteIdsFlow) { result, favoriteIds ->
            when (result) {
                is Result.Success -> {
                    Result.Success(result.data.map { movie ->
                        movie.copy(isFavorite = favoriteIds.contains(movie.id))
                    })
                }
                else -> result
            }
        }.onStart { emit(Result.Loading) }
    }

    override suspend fun getMovieDetail(movieId: Int): Flow<Result<Movie>> {
        val favoriteStatusFlow = localDataSource.isFavoriteMovie(movieId).distinctUntilChanged()
        
        val movieFlow = flow {
            try {
                val remoteResult = remoteDataSource.getMovieDetail(movieId)
                emit(remoteResult)
            } catch (e: Exception) {
                emit(Result.Error(com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Unknown(
                    e.message ?: "An unknown error occurred", e
                )))
            }
        }

        return combine(movieFlow, favoriteStatusFlow) { result, isFavorite ->
            when (result) {
                is Result.Success -> Result.Success(result.data.copy(isFavorite = isFavorite))
                else -> result
            }
        }.onStart { emit(Result.Loading) }
    }

    override suspend fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> {
        val favoriteIdsFlow = localDataSource.getFavoriteMovies()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .distinctUntilChanged()

        val searchFlow = flow {
            try {
                val result = remoteDataSource.searchMovies(query, page)
                emit(result)
            } catch (e: Exception) {
                emit(Result.Error(com.nadhifhayazee.moviecatalogue.core.util.NetworkException.Unknown(
                    e.message ?: "An unknown error occurred", e
                )))
            }
        }

        return combine(searchFlow, favoriteIdsFlow) { result, favoriteIds ->
            when (result) {
                is Result.Success -> {
                    Result.Success(result.data.map { movie ->
                        movie.copy(isFavorite = favoriteIds.contains(movie.id))
                    })
                }
                else -> result
            }
        }.onStart { emit(Result.Loading) }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return localDataSource.getFavoriteMovies().map { favoriteMovies ->
           favoriteMovies.map { it.toDomain() }
        }
    }

    override suspend fun addFavoriteMovie(movie: Movie) {
        localDataSource.insertFavorite(movie.toFavoriteEntity())
    }

    override suspend fun removeFavoriteMovie(movieId: Int) {
        localDataSource.deleteFavoriteById(movieId)
    }

    override fun isFavoriteMovie(movieId: Int): Flow<Boolean> {
        return localDataSource.isFavoriteMovie(movieId)
    }
}

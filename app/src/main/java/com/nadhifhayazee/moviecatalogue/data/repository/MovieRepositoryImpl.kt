package com.nadhifhayazee.moviecatalogue.data.repository

import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.data.local.LocalDataSource
import com.nadhifhayazee.moviecatalogue.data.local.entity.toEntity
import com.nadhifhayazee.moviecatalogue.data.remote.RemoteDataSource
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : MovieRepository {

    override suspend fun getLatestMovies(page: Int): Flow<Result<List<Movie>>> {
        android.util.Log.d("MovieRepository", "getLatestMovies: Starting page $page")
        return remoteDataSource.getLatestMovies(page)
            .combine(localDataSource.getFavoriteMovies()) { result, favorites ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("MovieRepository", "getLatestMovies: Got ${result.data.size} movies from remote, ${favorites.size} favorites")
                        val favoriteIds = favorites.map { it.id }.toSet()
                        val movies = result.data.map { movie ->
                            movie.copy(isFavorite = favoriteIds.contains(movie.id))
                        }
                        android.util.Log.d("MovieRepository", "getLatestMovies: After updating favorites, ${movies.size} movies")
                        Result.Success(movies)
                    }
                    is Result.Error -> result
                    is Result.Loading -> result
                }
            }
    }

    override suspend fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> {
        android.util.Log.d("MovieRepository", "getTopRatedMovies: Starting page $page")
        return remoteDataSource.getTopRatedMovies(page)
            .combine(localDataSource.getFavoriteMovies()) { result, favorites ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("MovieRepository", "getTopRatedMovies: Got ${result.data.size} movies from remote, ${favorites.size} favorites")
                        val favoriteIds = favorites.map { it.id }.toSet()
                        val movies = result.data.map { movie ->
                            movie.copy(isFavorite = favoriteIds.contains(movie.id))
                        }
                        Result.Success(movies)
                    }
                    is Result.Error -> result
                    is Result.Loading -> result
                }
            }
    }

    override suspend fun getRecommendedMovies(page: Int): Flow<Result<List<Movie>>> {
        android.util.Log.d("MovieRepository", "getRecommendedMovies: Starting page $page")
        return remoteDataSource.getRecommendedMovies(page)
            .combine(localDataSource.getFavoriteMovies()) { result, favorites ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("MovieRepository", "getRecommendedMovies: Got ${result.data.size} movies from remote, ${favorites.size} favorites")
                        val favoriteIds = favorites.map { it.id }.toSet()
                        val movies = result.data.map { movie ->
                            movie.copy(isFavorite = favoriteIds.contains(movie.id))
                        }
                        Result.Success(movies)
                    }
                    is Result.Error -> result
                    is Result.Loading -> result
                }
            }
    }

    
    override suspend fun getMovieDetail(movieId: Int): Flow<Result<Movie>> {
        return remoteDataSource.getMovieDetail(movieId)
            .combine(localDataSource.isFavoriteMovie(movieId)) { result, isFavorite ->
                when (result) {
                    is Result.Success -> {
                        Result.Success(result.data.copy(isFavorite = isFavorite))
                    }
                    is Result.Error -> {
                        Result.Error(result.message)
                    }
                    is Result.Loading -> {
                        Result.Loading
                    }
                }
            }
            .catch { e ->
                emit(Result.Error(e.message ?: "An unknown error occurred"))
            }
    }

    override suspend fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> {
        android.util.Log.d("MovieRepository", "searchMovies: Starting search for '$query' page $page")
        return remoteDataSource.searchMovies(query, page)
            .combine(localDataSource.getFavoriteMovies()) { result, favorites ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("MovieRepository", "searchMovies: Got ${result.data.size} movies from search, ${favorites.size} favorites")
                        val favoriteIds = favorites.map { it.id }.toSet()
                        val movies = result.data.map { movie ->
                            movie.copy(isFavorite = favoriteIds.contains(movie.id))
                        }
                        Result.Success(movies)
                    }
                    is Result.Error -> result
                    is Result.Loading -> result
                }
            }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return localDataSource.getFavoriteMovies()
    }

    override suspend fun addFavoriteMovie(movie: Movie) {
        localDataSource.insertFavorite(movie.toEntity())
    }

    override suspend fun removeFavoriteMovie(movieId: Int) {
        localDataSource.deleteFavoriteById(movieId)
    }

    override fun isFavoriteMovie(movieId: Int): Flow<Boolean> {
        return localDataSource.isFavoriteMovie(movieId)
    }
}

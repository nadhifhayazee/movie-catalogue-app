package com.nadhifhayazee.moviecatalogue.data.local.dao

import androidx.room.*
import com.nadhifhayazee.moviecatalogue.data.local.entity.FavoriteMovieEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.CachedMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    // Favorite movies methods
    @Query("SELECT * FROM favorite_movies")
    fun getFavoriteMovies(): Flow<List<FavoriteMovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :movieId)")
    fun isFavoriteMovie(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovieEntity)

    @Delete
    suspend fun deleteFavorite(movie: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies WHERE id = :movieId")
    suspend fun deleteFavoriteById(movieId: Int)

    // Cached movies methods for offline support
    @Query("SELECT * FROM cached_movies WHERE category = :category ORDER BY cache_timestamp DESC")
    fun getCachedMoviesByCategory(category: String): Flow<List<CachedMovieEntity>>

    @Query("SELECT * FROM cached_movies WHERE category = :category ORDER BY cache_timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getCachedMoviesByCategoryPaged(category: String, limit: Int, offset: Int): List<CachedMovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMovies(movies: List<CachedMovieEntity>)

    @Query("DELETE FROM cached_movies WHERE category = :category")
    suspend fun clearCachedMovies(category: String)

    @Query("DELETE FROM cached_movies WHERE cache_timestamp < :timestamp")
    suspend fun clearOldCache(timestamp: Long)
}

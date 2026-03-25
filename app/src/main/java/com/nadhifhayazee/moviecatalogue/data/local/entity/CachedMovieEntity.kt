package com.nadhifhayazee.moviecatalogue.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    @ColumnInfo(name = "poster_path")
    val posterPath: String?,
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String?,
    val overview: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: String,
    @ColumnInfo(name = "vote_average")
    val voteAverage: Double,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "cache_timestamp")
    val cacheTimestamp: Long = System.currentTimeMillis()
)

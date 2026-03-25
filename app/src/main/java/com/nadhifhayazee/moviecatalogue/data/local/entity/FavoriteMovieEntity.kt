package com.nadhifhayazee.moviecatalogue.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
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
    val voteAverage: Double
)

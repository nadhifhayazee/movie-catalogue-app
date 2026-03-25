package com.nadhifhayazee.moviecatalogue.data.local.entity

import com.nadhifhayazee.moviecatalogue.domain.model.Movie

fun FavoriteMovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        isFavorite = true // If it's in favorite_movies table, it's a favorite
    )
}

fun Movie.toFavoriteEntity(): FavoriteMovieEntity {
    return FavoriteMovieEntity(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage
    )
}

fun CachedMovieEntity.toDomain(isFavorite: Boolean = false): Movie {
    return Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        isFavorite = isFavorite
    )
}

fun Movie.toCachedEntity(category: String): CachedMovieEntity {
    return CachedMovieEntity(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        category = category,
        cacheTimestamp = System.currentTimeMillis()
    )
}

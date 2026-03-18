package com.nadhifhayazee.moviecatalogue.core.util

import com.nadhifhayazee.moviecatalogue.BuildConfig

object Constants {
    const val BASE_URL = "https://api.themoviedb.org/3/"
    val API_KEY = BuildConfig.TMDB_API_KEY
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/original"
}

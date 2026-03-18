package com.nadhifhayazee.moviecatalogue.domain.usecase

import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteMoviesUseCase(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return repository.getFavoriteMovies()
    }
}

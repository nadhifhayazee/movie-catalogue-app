package com.nadhifhayazee.moviecatalogue.domain.usecase

import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import javax.inject.Inject

class RemoveFavoriteMovieUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int) {
        repository.removeFavoriteMovie(movieId)
    }
}

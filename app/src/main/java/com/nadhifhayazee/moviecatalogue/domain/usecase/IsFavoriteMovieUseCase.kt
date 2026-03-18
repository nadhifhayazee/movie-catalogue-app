package com.nadhifhayazee.moviecatalogue.domain.usecase

import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavoriteMovieUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(movieId: Int): Flow<Boolean> {
        return repository.isFavoriteMovie(movieId)
    }
}

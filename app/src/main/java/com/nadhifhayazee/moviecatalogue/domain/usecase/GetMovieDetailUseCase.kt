package com.nadhifhayazee.moviecatalogue.domain.usecase

import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetMovieDetailUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): Flow<Result<Movie>> {
        return repository.getMovieDetail(movieId)
    }
}

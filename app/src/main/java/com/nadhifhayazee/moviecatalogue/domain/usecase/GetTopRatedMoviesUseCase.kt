package com.nadhifhayazee.moviecatalogue.domain.usecase

import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetTopRatedMoviesUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(page: Int = 1): Flow<Result<List<Movie>>> {
        return repository.getTopRatedMovies(page)
    }
}

package com.nadhifhayazee.moviecatalogue.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class RepositoryMovieQuery {
    data object Latest : RepositoryMovieQuery()
    data object TopRated : RepositoryMovieQuery()
    data object Recommended : RepositoryMovieQuery()
    data class Search(val query: String) : RepositoryMovieQuery()
}

class RepositoryMoviePagingSource @Inject constructor(
    private val repository: MovieRepository,
    private val query: RepositoryMovieQuery
) : PagingSource<Int, Movie>() {

    class Factory @Inject constructor(
        private val repository: MovieRepository
    ) {
        fun create(query: RepositoryMovieQuery): RepositoryMoviePagingSource {
            return RepositoryMoviePagingSource(repository, query)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val resultFlow = when (query) {
                is RepositoryMovieQuery.Latest -> repository.getLatestMovies(page)
                is RepositoryMovieQuery.TopRated -> repository.getTopRatedMovies(page)
                is RepositoryMovieQuery.Recommended -> repository.getRecommendedMovies(page)
                is RepositoryMovieQuery.Search -> repository.searchMovies(query.query, page)
            }

            val result = resultFlow.first()

            return when (result) {
                is Result.Success -> {
                    val movies = result.data
                    val nextKey = if (movies.isEmpty() || movies.size < pageSize) null else page + 1
                    val prevKey = if (page == 1) null else page - 1

                    LoadResult.Page(
                        data = movies,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                is Result.Error -> {
                    LoadResult.Error(Throwable(result.message))
                }
                is Result.Loading -> {
                    // This shouldn't happen since we're using .first()
                    LoadResult.Error(Throwable("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
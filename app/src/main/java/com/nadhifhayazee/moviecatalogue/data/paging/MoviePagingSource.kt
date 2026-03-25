package com.nadhifhayazee.moviecatalogue.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.data.remote.RemoteDataSource
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import javax.inject.Inject

sealed class MovieQuery {
    data object Latest : MovieQuery()
    data object TopRated : MovieQuery()
    data object Recommended : MovieQuery()
    data class Search(val query: String) : MovieQuery()
}

class MoviePagingSource @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val query: MovieQuery
) : PagingSource<Int, Movie>() {

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

            val result = when (query) {
                is MovieQuery.Latest -> remoteDataSource.getLatestMovies(page)
                is MovieQuery.TopRated -> remoteDataSource.getTopRatedMovies(page)
                is MovieQuery.Recommended -> remoteDataSource.getRecommendedMovies(page)
                is MovieQuery.Search -> remoteDataSource.searchMovies(query.query, page)
            }

            return when (result) {
                is Result.Success -> {
                    val movies = result.data
                    val nextKey = if (movies.size < pageSize) null else page + 1
                    val prevKey = if (page == 1) null else page - 1

                    LoadResult.Page(
                        data = movies,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                is Result.Error -> {
                    LoadResult.Error(result.exception)
                }
                is Result.Loading -> {
                    // This shouldn't happen since we're using suspend functions
                    LoadResult.Error(Throwable("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

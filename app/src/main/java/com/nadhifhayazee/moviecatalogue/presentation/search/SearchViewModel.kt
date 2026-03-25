package com.nadhifhayazee.moviecatalogue.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val addFavoriteMovieUseCase: AddFavoriteMovieUseCase,
    private val removeFavoriteMovieUseCase: RemoveFavoriteMovieUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage = 1
    private var currentQuery = ""

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.Search -> {
                performSearch(event.query, reset = true)
            }
            is SearchUiEvent.LoadMore -> {
                loadMoreResults()
            }
            is SearchUiEvent.ToggleFavorite -> {
                onToggleFavorite(event.movie)
            }
            SearchUiEvent.ClearSearch -> {
                clearSearch()
            }
        }
    }

    private fun performSearch(query: String, reset: Boolean = false) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        currentQuery = query
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            if (reset) {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        movies = emptyList(),
                        hasMore = true
                    )
                }
                currentPage = 1
            }

            searchMoviesUseCase(query, currentPage).collect { result ->
                when (result) {
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Loading -> {
                        if (_uiState.value.movies.isEmpty()) {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Success -> {
                        _uiState.update { state ->
                            val updatedMovies = if (reset) {
                                result.data
                            } else {
                                state.movies.take((currentPage - 1) * 20) + result.data
                            }

                            state.copy(
                                movies = updatedMovies,
                                isLoading = false,
                                hasMore = result.data.isNotEmpty(),
                                error = null
                            )
                        }
                    }
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadMoreResults() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return

        currentPage++
        performSearch(currentQuery, reset = false)
    }

    private fun onToggleFavorite(movie: Movie) {
        viewModelScope.launch {
            if (movie.isFavorite) {
                removeFavoriteMovieUseCase(movie.id)
            } else {
                addFavoriteMovieUseCase(movie)
            }
            // No manual update needed as repository flow is reactive
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        currentQuery = ""
        currentPage = 1
        _uiState.value = SearchUiState()
    }
}

data class SearchUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: NetworkException? = null,
    val hasMore: Boolean = true
)

sealed class SearchUiEvent {
    data class Search(val query: String) : SearchUiEvent()
    data object LoadMore : SearchUiEvent()
    data class ToggleFavorite(val movie: Movie) : SearchUiEvent()
    data object ClearSearch : SearchUiEvent()
}
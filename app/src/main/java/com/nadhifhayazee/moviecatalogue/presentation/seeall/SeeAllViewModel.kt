package com.nadhifhayazee.moviecatalogue.presentation.seeall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetLatestMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetRecommendedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetTopRatedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeeAllViewModel @Inject constructor(
    private val getLatestMoviesUseCase: GetLatestMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val addFavoriteMovieUseCase: AddFavoriteMovieUseCase,
    private val removeFavoriteMovieUseCase: RemoveFavoriteMovieUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val category: String = checkNotNull(savedStateHandle["category"])
    private var currentPage = 1
    private var collectionJob: Job? = null

    private val _uiState = MutableStateFlow(SeeAllUiState())
    val uiState: StateFlow<SeeAllUiState> = _uiState.asStateFlow()

    init {
        loadMovies(reset = true)
    }

    fun onEvent(event: SeeAllUiEvent) {
        when (event) {
            is SeeAllUiEvent.LoadMore -> {
                loadMoreMovies()
            }
            is SeeAllUiEvent.ToggleFavorite -> {
                onToggleFavorite(event.movie)
            }
            SeeAllUiEvent.Refresh -> {
                loadMovies(reset = true)
            }
        }
    }

    private fun loadMovies(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            collectionJob?.cancel()
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    movies = emptyList(),
                    hasMore = true
                )
            }
        }

        collectionJob = viewModelScope.launch {
            val flow = when (category) {
                MovieCategory.LATEST.name -> getLatestMoviesUseCase(currentPage)
                MovieCategory.TOP_RATED.name -> getTopRatedMoviesUseCase(currentPage)
                MovieCategory.RECOMMENDED.name -> getRecommendedMoviesUseCase(currentPage)
                else -> return@launch
            }

            flow.collect { result ->
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
                                // For pagination, we need to handle how to append while keeping reactivity
                                // Actually, with reactive flows from repository, each result.data 
                                // will be the FULL list for that page.
                                // But Repository currently returns only the requested page.
                                // If we want to append, we'd normally use Paging 3 or manage the list here.
                                // Given current repo structure, we'll keep the list management here.
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

    private fun loadMoreMovies() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return

        currentPage++
        loadMovies(reset = false)
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
}

data class SeeAllUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: NetworkException? = null,
    val hasMore: Boolean = true
)

sealed class SeeAllUiEvent {
    data object LoadMore : SeeAllUiEvent()
    data class ToggleFavorite(val movie: Movie) : SeeAllUiEvent()
    data object Refresh : SeeAllUiEvent()
}
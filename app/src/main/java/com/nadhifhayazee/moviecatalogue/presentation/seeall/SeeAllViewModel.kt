package com.nadhifhayazee.moviecatalogue.presentation.seeall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetLatestMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetRecommendedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetTopRatedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    movies = emptyList(),
                    hasMore = true
                )
            }
        }

        viewModelScope.launch {
            val flow = when (category) {
                MovieCategory.LATEST.name -> getLatestMoviesUseCase(currentPage)
                MovieCategory.TOP_RATED.name -> getTopRatedMoviesUseCase(currentPage)
                MovieCategory.RECOMMENDED.name -> getRecommendedMoviesUseCase(currentPage)
                else -> return@launch
            }

            flow.collect { result ->
                when (result) {
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Success -> {
                        _uiState.update { state ->
                            val updatedMovies = if (reset) {
                                result.data
                            } else {
                                state.movies + result.data
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
                                error = result.message
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

            // Update local state immediately for better UX
            _uiState.update { state ->
                state.copy(
                    movies = state.movies.map {
                        if (it.id == movie.id) {
                            it.copy(isFavorite = !movie.isFavorite)
                        } else it
                    }
                )
            }
        }
    }
}

data class SeeAllUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)

sealed class SeeAllUiEvent {
    data object LoadMore : SeeAllUiEvent()
    data class ToggleFavorite(val movie: Movie) : SeeAllUiEvent()
    data object Refresh : SeeAllUiEvent()
}
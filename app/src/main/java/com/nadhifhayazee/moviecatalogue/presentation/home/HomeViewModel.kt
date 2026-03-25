package com.nadhifhayazee.moviecatalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetLatestMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetRecommendedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetTopRatedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLatestMoviesUseCase: GetLatestMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val addFavoriteMovieUseCase: AddFavoriteMovieUseCase,
    private val removeFavoriteMovieUseCase: RemoveFavoriteMovieUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val stateUpdateMutex = Mutex()
    private var collectionJob: Job? = null

    init {
        loadMovies()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> loadMovies()
            is HomeUiEvent.ToggleFavorite -> onToggleFavorite(event.movie)
        }
    }

    private fun onToggleFavorite(movie: Movie) {
        viewModelScope.launch {
            if (movie.isFavorite) {
                removeFavoriteMovieUseCase(movie.id)
            } else {
                addFavoriteMovieUseCase(movie)
            }
        }
    }

    private fun loadMovies() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Start all collections in parallel
            launch { collectMovieFlow(getLatestMoviesUseCase(1), MovieCategory.LATEST) }
            launch { collectMovieFlow(getTopRatedMoviesUseCase(1), MovieCategory.TOP_RATED) }
            launch { collectMovieFlow(getRecommendedMoviesUseCase(1), MovieCategory.RECOMMENDED) }
        }
    }

    private suspend fun collectMovieFlow(
        flow: Flow<Result<List<Movie>>>,
        category: MovieCategory
    ) {
        flow.collect { result ->
            stateUpdateMutex.withLock {
                when (result) {
                    is Result.Success -> {
                        val currentState = _uiState.value
                        _uiState.value = when (category) {
                            MovieCategory.LATEST -> currentState.copy(latestMovies = result.data, isLoading = false)
                            MovieCategory.TOP_RATED -> currentState.copy(topRatedMovies = result.data, isLoading = false)
                            MovieCategory.RECOMMENDED -> currentState.copy(recommendedMovies = result.data, isLoading = false)
                            MovieCategory.FAVORITES -> currentState
                        }
                    }
                    is Result.Error -> {
                        if (_uiState.value.error == null) {
                            _uiState.value = _uiState.value.copy(error = result.exception, isLoading = false)
                        }
                    }
                    is Result.Loading -> {
                        // Keep loading state if we don't have data for this category yet
                        val currentState = _uiState.value
                        val hasData = when (category) {
                            MovieCategory.LATEST -> currentState.latestMovies.isNotEmpty()
                            MovieCategory.TOP_RATED -> currentState.topRatedMovies.isNotEmpty()
                            MovieCategory.RECOMMENDED -> currentState.recommendedMovies.isNotEmpty()
                            MovieCategory.FAVORITES -> true
                        }
                        if (!hasData) {
                            _uiState.value = currentState.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }
}

data class HomeUiState(
    val latestMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: NetworkException? = null
)

sealed class HomeUiEvent {
    data object Refresh : HomeUiEvent()
    data class ToggleFavorite(val movie: Movie) : HomeUiEvent()
}

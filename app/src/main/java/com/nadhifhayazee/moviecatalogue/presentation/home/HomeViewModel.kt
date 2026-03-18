package com.nadhifhayazee.moviecatalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.core.util.Result
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetLatestMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetRecommendedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetTopRatedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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

    init {
        loadMovies()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> {
                viewModelScope.launch {
                    clearErrorState()
                    loadMovies()
                }
            }
            is HomeUiEvent.ToggleFavorite -> onToggleFavorite(event.movie)
        }
    }

    private suspend fun clearErrorState() {
        stateUpdateMutex.withLock {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    private fun onToggleFavorite(movie: Movie) {
        viewModelScope.launch {
            if (movie.isFavorite) {
                removeFavoriteMovieUseCase(movie.id)
                updateMovieFavoriteStatus(movie.id, false)
            } else {
                addFavoriteMovieUseCase(movie)
                updateMovieFavoriteStatus(movie.id, true)
            }
        }
    }

    private suspend fun updateMovieFavoriteStatus(movieId: Int, isFavorite: Boolean) {
        stateUpdateMutex.withLock {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                latestMovies = updateMovieList(currentState.latestMovies, movieId, isFavorite),
                topRatedMovies = updateMovieList(currentState.topRatedMovies, movieId, isFavorite),
                recommendedMovies = updateMovieList(currentState.recommendedMovies, movieId, isFavorite)
            )
        }
    }

    private fun updateMovieList(movies: List<Movie>, movieId: Int, isFavorite: Boolean): List<Movie> {
        return movies.map { if (it.id == movieId) it.copy(isFavorite = isFavorite) else it }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Launch all three flows concurrently
                val latestJob = launch { collectLatestMovies() }
                val topRatedJob = launch { collectTopRatedMovies() }
                val recommendedJob = launch { collectRecommendedMovies() }
                
                // Wait for all collections to complete
                latestJob.join()
                topRatedJob.join()
                recommendedJob.join()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun collectLatestMovies() {
        collectMovieFlow(getLatestMoviesUseCase(1), MovieCategory.LATEST)
    }

    private suspend fun collectTopRatedMovies() {
        collectMovieFlow(getTopRatedMoviesUseCase(1), MovieCategory.TOP_RATED)
    }

    private suspend fun collectRecommendedMovies() {
        collectMovieFlow(getRecommendedMoviesUseCase(1), MovieCategory.RECOMMENDED)
    }

    private suspend fun collectMovieFlow(
        flow: kotlinx.coroutines.flow.Flow<Result<List<Movie>>>,
        category: MovieCategory
    ) {
        flow.collect { result ->
            stateUpdateMutex.withLock {
                when (result) {
                    is Result.Success -> {
                        val currentState = _uiState.value
                        _uiState.value = when (category) {
                            MovieCategory.LATEST -> currentState.copy(latestMovies = result.data)
                            MovieCategory.TOP_RATED -> currentState.copy(topRatedMovies = result.data)
                            MovieCategory.RECOMMENDED -> currentState.copy(recommendedMovies = result.data)
                            MovieCategory.FAVORITES -> currentState // FAVORITES not used in HomeViewModel
                        }
                    }
                    is Result.Error -> {
                        if (_uiState.value.error.isNullOrEmpty()) {
                            _uiState.value = _uiState.value.copy(error = result.message)
                        }
                    }
                    is Result.Loading -> {
                        // Loading state is already handled by isLoading flag
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
    val error: String? = null
)

sealed class HomeUiEvent {
    data object Refresh : HomeUiEvent()
    data class ToggleFavorite(val movie: Movie) : HomeUiEvent()
}

package com.nadhifhayazee.moviecatalogue.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetFavoriteMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteMoviesUseCase: GetFavoriteMoviesUseCase,
    private val removeFavoriteMovieUseCase: com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            getFavoriteMoviesUseCase().collect { movies ->
                _uiState.value = _uiState.value.copy(
                    movies = movies,
                    isLoading = false
                )
            }
        }
    }

    fun onRemoveFavorite(movieId: Int) {
        viewModelScope.launch {
            removeFavoriteMovieUseCase(movieId)
            // Update UI immediately - the Flow will also update from the database
            _uiState.value = _uiState.value.copy(
                movies = _uiState.value.movies.filter { it.id != movieId }
            )
        }
    }
}

data class FavoritesUiState(
    val movies: List<com.nadhifhayazee.moviecatalogue.domain.model.Movie> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

package com.nadhifhayazee.moviecatalogue.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetMovieDetailUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.IsFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val addFavoriteMovieUseCase: AddFavoriteMovieUseCase,
    private val removeFavoriteMovieUseCase: RemoveFavoriteMovieUseCase,
    private val isFavoriteMovieUseCase: IsFavoriteMovieUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
        checkFavoriteStatus()
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.ToggleFavorite -> toggleFavorite()
            DetailUiEvent.Refresh -> loadMovieDetail()
        }
    }

    private fun loadMovieDetail() {
        viewModelScope.launch {
            getMovieDetailUseCase(movieId).collect { result ->
                when (result) {
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            movie = result.data,
                            isLoading = false
                        )
                        checkFavoriteStatus()
                    }
                    is com.nadhifhayazee.moviecatalogue.core.util.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.exception,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            val isFavorite = isFavoriteMovieUseCase(movieId).first()
            _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
        }
    }

    private fun toggleFavorite() {
        val movie = _uiState.value.movie ?: return

        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                removeFavoriteMovieUseCase(movieId)
                _uiState.value = _uiState.value.copy(isFavorite = false)
            } else {
                addFavoriteMovieUseCase(movie)
                _uiState.value = _uiState.value.copy(isFavorite = true)
            }
        }
    }
}

data class DetailUiState(
    val movie: com.nadhifhayazee.moviecatalogue.domain.model.Movie? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: NetworkException? = null
)

sealed class DetailUiEvent {
    data object ToggleFavorite : DetailUiEvent()
    data object Refresh : DetailUiEvent()
}

package com.nadhifhayazee.moviecatalogue.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nadhifhayazee.moviecatalogue.data.paging.RepositoryMoviePagingSource
import com.nadhifhayazee.moviecatalogue.data.paging.RepositoryMovieQuery
import com.nadhifhayazee.moviecatalogue.domain.model.Movie
import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PagingSearchViewModel @Inject constructor(
    private val pagingSourceFactory: RepositoryMoviePagingSource.Factory,
    private val addFavoriteMovieUseCase: AddFavoriteMovieUseCase,
    private val removeFavoriteMovieUseCase: RemoveFavoriteMovieUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val movies: Flow<PagingData<Movie>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            // Return empty paging data when no search query
            kotlinx.coroutines.flow.flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    pagingSourceFactory.create(RepositoryMovieQuery.Search(query))
                }
            ).flow.cachedIn(viewModelScope)
        }
    }

    fun onEvent(event: PagingSearchUiEvent) {
        when (event) {
            is PagingSearchUiEvent.Search -> {
                _searchQuery.value = event.query
            }
            is PagingSearchUiEvent.ToggleFavorite -> {
                onToggleFavorite(event.movie)
            }
            PagingSearchUiEvent.ClearSearch -> {
                _searchQuery.value = ""
            }
            PagingSearchUiEvent.ActivateSearch -> {
                _isSearchActive.value = true
            }
            PagingSearchUiEvent.DeactivateSearch -> {
                _isSearchActive.value = false
                _searchQuery.value = ""
            }
        }
    }

    private fun onToggleFavorite(movie: Movie) {
        viewModelScope.launch {
            if (movie.isFavorite) {
                removeFavoriteMovieUseCase(movie.id)
            } else {
                addFavoriteMovieUseCase(movie)
            }
            // Note: With Paging 3, we can't easily update individual items in the paging stream
            // In a real app, we'd use Room as single source of truth with RemoteMediator
            // For now, the favorite status will update when the screen is refreshed
        }
    }
}

sealed class PagingSearchUiEvent {
    data class Search(val query: String) : PagingSearchUiEvent()
    data class ToggleFavorite(val movie: Movie) : PagingSearchUiEvent()
    data object ClearSearch : PagingSearchUiEvent()
    data object ActivateSearch : PagingSearchUiEvent()
    data object DeactivateSearch : PagingSearchUiEvent()
}
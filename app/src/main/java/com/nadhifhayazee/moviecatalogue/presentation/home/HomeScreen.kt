package com.nadhifhayazee.moviecatalogue.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.moviecatalogue.presentation.components.LoadingContent
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie Catalogue") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorites"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.latestMovies.isEmpty()) {
                LoadingContent()
            } else if (uiState.error != null) {
                com.nadhifhayazee.moviecatalogue.presentation.components.ErrorContent(
                    exception = uiState.error,
                    onRetry = { viewModel.onEvent(HomeUiEvent.Refresh) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        MovieSection(
                            title = "Latest Movies",
                            movies = uiState.latestMovies,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { onSeeAllClick(com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.LATEST.name) },
                            onFavoriteClick = { viewModel.onEvent(HomeUiEvent.ToggleFavorite(it)) }
                        )
                    }

                    item {
                        MovieSection(
                            title = "Top Rated",
                            movies = uiState.topRatedMovies,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { onSeeAllClick(com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.TOP_RATED.name) },
                            onFavoriteClick = { viewModel.onEvent(HomeUiEvent.ToggleFavorite(it)) }
                        )
                    }

                    item {
                        MovieSection(
                            title = "Recommended",
                            movies = uiState.recommendedMovies,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { onSeeAllClick(com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.RECOMMENDED.name) },
                            onFavoriteClick = { viewModel.onEvent(HomeUiEvent.ToggleFavorite(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieSection(
    title: String,
    movies: List<com.nadhifhayazee.moviecatalogue.domain.model.Movie>,
    onMovieClick: (Int) -> Unit,
    onSeeAllClick: () -> Unit,
    onFavoriteClick: (com.nadhifhayazee.moviecatalogue.domain.model.Movie) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    onFavoriteClick = { onFavoriteClick(movie) },
                    showFavoriteButton = true
                )
            }
        }
    }
}

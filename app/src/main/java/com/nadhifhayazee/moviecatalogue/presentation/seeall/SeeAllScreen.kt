package com.nadhifhayazee.moviecatalogue.presentation.seeall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nadhifhayazee.moviecatalogue.presentation.components.ErrorContent
import com.nadhifhayazee.moviecatalogue.presentation.components.LoadingContent
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllScreen(
    category: String,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: SeeAllViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridState()

    // Load more when reaching end of list
    LaunchedEffect(lazyGridState) {
        while (true) {
            val visibleItems = lazyGridState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val lastVisibleItem = visibleItems.last()
                val totalItems = lazyGridState.layoutInfo.totalItemsCount
                
                if (lastVisibleItem.index >= totalItems - 5 && !uiState.isLoading && uiState.hasMore) {
                    viewModel.onEvent(SeeAllUiEvent.LoadMore)
                }
            }
            delay(100) // Check every 100ms
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (category) {
                            com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.LATEST.name -> "Latest Movies"
                            com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.TOP_RATED.name -> "Top Rated"
                            com.nadhifhayazee.moviecatalogue.domain.model.MovieCategory.RECOMMENDED.name -> "Recommended"
                            else -> "Movies"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
            when {
                uiState.isLoading && uiState.movies.isEmpty() -> {
                    LoadingContent()
                }
                
                uiState.error != null && uiState.movies.isEmpty() -> {
                    ErrorContent(
                        message = uiState.error,
                        onRetry = { viewModel.onEvent(SeeAllUiEvent.Refresh) }
                    )
                }
                
                else -> {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.movies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onFavoriteClick = {
                                    viewModel.onEvent(SeeAllUiEvent.ToggleFavorite(movie))
                                },
                                showFavoriteButton = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Loading indicator at bottom for pagination
                        if (uiState.isLoading && uiState.movies.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        // End of list indicator
                        if (!uiState.hasMore && uiState.movies.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = "No more movies to load",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
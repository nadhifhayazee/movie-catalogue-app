package com.nadhifhayazee.moviecatalogue.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nadhifhayazee.moviecatalogue.presentation.components.ErrorContent
import com.nadhifhayazee.moviecatalogue.presentation.components.LoadingContent
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieCard
import com.nadhifhayazee.moviecatalogue.presentation.components.SearchTextField
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Load more when reaching end of list
    LaunchedEffect(lazyListState) {
        while (true) {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val lastVisibleItem = visibleItems.last()
                val totalItems = lazyListState.layoutInfo.totalItemsCount
                
                if (lastVisibleItem.index >= totalItems - 5 && !uiState.isLoading && uiState.hasMore) {
                    viewModel.onEvent(SearchUiEvent.LoadMore)
                }
            }
            delay(100) // Check every 100ms
        }
    }

    // Focus search field when search becomes active
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100) // Small delay to ensure the TextField is composed and attached
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: IllegalStateException) {
                // FocusRequester might still not be initialized
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchTextField(
                            query = searchQuery,
                            onQueryChange = { newQuery ->
                                searchQuery = newQuery
                                viewModel.onEvent(SearchUiEvent.Search(newQuery))
                            },
                            onClearClick = {
                                searchQuery = ""
                                viewModel.onEvent(SearchUiEvent.ClearSearch)
                            },
                            focusRequester = focusRequester,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Search Movies")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
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
                searchQuery.isEmpty() && !isSearchActive -> {
                    // Show empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Search for movies",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enter a movie title to find what you're looking for",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.isLoading && uiState.movies.isEmpty() -> {
                    LoadingContent()
                }

                uiState.error != null && uiState.movies.isEmpty() -> {
                    ErrorContent(
                        exception = uiState.error,
                        onRetry = { viewModel.onEvent(SearchUiEvent.Search(searchQuery)) }
                    )
                }

                uiState.movies.isEmpty() && searchQuery.isNotEmpty() -> {
                    // No results found
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No results",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No movies found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.movies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onFavoriteClick = {
                                    viewModel.onEvent(SearchUiEvent.ToggleFavorite(movie))
                                },
                                showFavoriteButton = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Loading indicator at bottom for pagination
                        if (uiState.isLoading && uiState.movies.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        // End of list indicator
                        if (!uiState.hasMore && uiState.movies.isNotEmpty()) {
                            item {
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

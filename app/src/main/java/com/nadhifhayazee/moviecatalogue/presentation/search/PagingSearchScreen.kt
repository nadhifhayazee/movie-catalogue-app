package com.nadhifhayazee.moviecatalogue.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieCard
import com.nadhifhayazee.moviecatalogue.presentation.components.SearchTextField
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagingSearchScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: PagingSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus search field when screen loads or search becomes active
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
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
                                viewModel.onEvent(PagingSearchUiEvent.Search(newQuery))
                            },
                            onClearClick = {
                                viewModel.onEvent(PagingSearchUiEvent.ClearSearch)
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
                        IconButton(onClick = { 
                            viewModel.onEvent(PagingSearchUiEvent.ActivateSearch) 
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
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
                !isSearchActive && searchQuery.isEmpty() -> {
                    // Show empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
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

                searchQuery.isEmpty() -> {
                    // Search active but no query yet
                    // Could show recent searches or suggestions here
                }

                else -> {
                    PagingMovieList(
                        movies = movies,
                        onMovieClick = onMovieClick,
                        onFavoriteClick = { movie ->
                            viewModel.onEvent(PagingSearchUiEvent.ToggleFavorite(movie))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PagingMovieList(
    movies: LazyPagingItems<com.nadhifhayazee.moviecatalogue.domain.model.Movie>,
    onMovieClick: (Int) -> Unit,
    onFavoriteClick: (com.nadhifhayazee.moviecatalogue.domain.model.Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            count = movies.itemCount,
            key = movies.itemKey { it.id }
        ) { index ->
            val movie = movies[index]
            if (movie != null) {
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    onFavoriteClick = { onFavoriteClick(movie) },
                    showFavoriteButton = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show loading indicator when loading more
        item {
            if (movies.loadState.append is androidx.paging.LoadState.Loading) {
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

        // Show error when loading more fails
        item {
            if (movies.loadState.append is androidx.paging.LoadState.Error) {
                val error = (movies.loadState.append as androidx.paging.LoadState.Error).error
                Text(
                    text = "Error loading more: ${error.message}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Show no more items indicator
        item {
            if (movies.loadState.append is androidx.paging.LoadState.NotLoading && 
                movies.itemCount > 0) {
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

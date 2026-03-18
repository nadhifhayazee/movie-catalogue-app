package com.nadhifhayazee.moviecatalogue.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.moviecatalogue.presentation.components.LoadingContent
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieDetailHeader
import com.nadhifhayazee.moviecatalogue.presentation.components.MovieDetailOverview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie Detail") },
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
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    com.nadhifhayazee.moviecatalogue.presentation.components.ErrorContent(
                        message = uiState.error,
                        onRetry = { viewModel.onEvent(DetailUiEvent.Refresh) }
                    )
                }
                uiState.movie != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MovieDetailHeader(
                            movie = uiState.movie!!,
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = {
                                viewModel.onEvent(DetailUiEvent.ToggleFavorite)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MovieDetailOverview(
                            overview = uiState.movie!!.overview
                        )
                    }
                }
            }
        }
    }
}

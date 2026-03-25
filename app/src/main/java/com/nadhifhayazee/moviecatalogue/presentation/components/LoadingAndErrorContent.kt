package com.nadhifhayazee.moviecatalogue.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nadhifhayazee.moviecatalogue.core.util.NetworkException

@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ErrorContent(
    exception: NetworkException?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = exception?.message ?: "An error occurred",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Retry")
            }
        }
    }
}

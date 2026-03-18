# Android Movie Catalogue --- Advanced Prompt (Production Level)

You are a **Senior Android Architect** who builds scalable production
Android apps.

Help me build a **Movie Catalogue Android application** using modern
Android architecture and best practices.

The code must be **clean, scalable, maintainable, and
production-ready**.

------------------------------------------------------------------------

# Tech Stack

Use the following technologies:

-   Kotlin
-   Jetpack Compose
-   Clean Architecture
-   MVVM + MVI style state management
-   Repository Pattern
-   Kotlin Coroutines
-   Kotlin Flow
-   Retrofit
-   OkHttp
-   Room Database
-   Hilt Dependency Injection
-   Navigation Compose
-   Material 3
-   Coil (for image loading)

Optional but recommended:

-   Paging 3
-   Result Wrapper
-   Network Error Handling
-   Offline caching strategy

------------------------------------------------------------------------

# Architecture

Use **Clean Architecture with 3 layers**

## Presentation Layer

Contains:

-   Compose Screens
-   ViewModels
-   UI State
-   UI Events
-   Navigation

State management must use **StateFlow**.

Use **Unidirectional Data Flow (UDF)**.

Example flow:

UI -\> Event -\> ViewModel -\> UseCase -\> Repository -\> DataSource

Each screen should define:

-   UiState
-   UiEvent
-   UiEffect (optional)

Example UiState:

``` kotlin
data class HomeUiState(
    val latestMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

------------------------------------------------------------------------

## Domain Layer

Contains pure business logic.

Must contain:

-   Domain Models
-   Repository Interfaces
-   UseCases

Example UseCases:

-   GetLatestMoviesUseCase
-   GetTopRatedMoviesUseCase
-   GetRecommendedMoviesUseCase
-   GetMovieDetailUseCase
-   GetFavoriteMoviesUseCase
-   AddFavoriteMovieUseCase
-   RemoveFavoriteMovieUseCase
-   IsFavoriteMovieUseCase

The Domain layer must **NOT depend on Android framework**.

------------------------------------------------------------------------

## Data Layer

Contains:

-   Repository Implementation
-   Remote Data Source
-   Local Data Source
-   DTO models
-   Entity models
-   Mapper functions

Repository should:

-   Fetch data from API
-   Cache favorites locally
-   Combine remote and local data

Return results using:

``` kotlin
Flow<Result<T>>
```

------------------------------------------------------------------------

# Result Wrapper

Create a sealed class to handle API results.

``` kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

------------------------------------------------------------------------

# Networking

Use **Retrofit + OkHttp**.

Base URL placeholder:

``` kotlin
const val BASE_URL = "YOUR_BASE_URL_HERE"
```

Create API endpoints for:

-   latest movies
-   top rated movies
-   recommended movies
-   movie detail

Use DTO models.

------------------------------------------------------------------------

# Local Database (Room)

Use Room to store **favorite movies**.

Create:

-   MovieEntity
-   MovieDao

DAO functions:

``` kotlin
insertFavorite(movie)

deleteFavorite(movie)

getFavoriteMovies(): Flow<List<MovieEntity>>

isFavorite(movieId): Flow<Boolean>
```

------------------------------------------------------------------------

# App Features

## 1. Home Screen

Sections:

-   Latest Movies
-   Top Rated Movies
-   Recommended Movies

Each section:

-   Horizontal movie list
-   "See All" button

Movie Card contains:

-   poster
-   title
-   rating
-   favorite button

Click opens **Movie Detail Screen**.

------------------------------------------------------------------------

## 2. Movie Detail Screen

Display:

-   Poster
-   Title
-   Rating
-   Release Date
-   Overview
-   Favorite Button

User can **add or remove favorite**.

------------------------------------------------------------------------

## 3. See All Screen

Shows full list of movies for a category.

Use:

-   LazyVerticalGrid or
-   LazyColumn

------------------------------------------------------------------------

## 4. Favorite Screen

Display all favorite movies stored in Room.

Data source:

``` kotlin
Flow<List<Movie>>
```

------------------------------------------------------------------------

# Navigation

Use **Navigation Compose**.

Routes:

    home
    movieDetail/{movieId}
    seeAll/{category}
    favorites

------------------------------------------------------------------------

# UI Design

Follow design patterns used by movie apps like:

-   Netflix
-   TMDB
-   IMDb

Guidelines:

-   large movie posters
-   horizontal scrolling sections
-   modern card UI
-   Material 3 design

Use:

-   LazyRow
-   LazyColumn
-   LazyVerticalGrid

------------------------------------------------------------------------

# Image Loading

Use **Coil**.

Example:

``` kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = movie.title
)
```

------------------------------------------------------------------------

# Project Structure

    com.example.moviecatalogue

    core
        network
        database
        util

    data
        remote
        local
        repository
        mapper
        model

    domain
        model
        repository
        usecase

    presentation
        home
        detail
        favorites
        seeall
        navigation
        components

------------------------------------------------------------------------

# What I want you to generate

Generate the project **step by step**:

1.  Project structure
2.  Gradle dependencies
3.  Domain models
4.  Retrofit API service
5.  DTO models
6.  Room database
7.  Repository implementation
8.  UseCases
9.  ViewModels
10. Compose UI Screens
11. Navigation setup
12. Movie Card UI component

Write each file separately and clearly labeled.

All code must follow **Android best practices and production
standards**.

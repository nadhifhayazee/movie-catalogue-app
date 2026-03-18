# Android Movie Catalogue - Agent Configuration

## Project Overview
This is a **Movie Catalogue Android application** built with modern Android architecture and best practices. The app displays movies from a movie database API, allows users to view details, and save favorites locally.

## Tech Stack
- **Kotlin** as the primary language
- **Jetpack Compose** for UI
- **Clean Architecture** with 3 layers (Presentation, Domain, Data)
- **MVVM + MVI** style state management
- **Repository Pattern**
- **Kotlin Coroutines & Flow** for asynchronous operations
- **Retrofit & OkHttp** for networking
- **Room Database** for local storage (favorites)
- **Hilt** for dependency injection
- **Navigation Compose** for navigation
- **Material 3** design system
- **Coil** for image loading

## Architecture Guidelines

### Clean Architecture Layers
1. **Presentation Layer**: Compose Screens, ViewModels, UI State/Events
2. **Domain Layer**: Pure business logic, UseCases, Repository interfaces
3. **Data Layer**: Repository implementations, Remote/Local data sources

### State Management
- Use **StateFlow** for UI state
- Implement **Unidirectional Data Flow (UDF)**
- Each screen should define: `UiState`, `UiEvent`, `UiEffect` (optional)
- Use `Result<T>` sealed class for handling API results

### File Structure Pattern
```
com.example.moviecatalogue
├── core
│   ├── network
│   ├── database
│   └── util
├── data
│   ├── remote
│   ├── local
│   ├── repository
│   ├── mapper
│   └── model
├── domain
│   ├── model
│   ├── repository
│   └── usecase
└── presentation
    ├── home
    ├── detail
    ├── favorites
    ├── seeall
    ├── navigation
    └── components
```

## Key Patterns to Follow

### 1. ViewModel Pattern
```kotlin
class HomeViewModel @Inject constructor(
    private val getLatestMoviesUseCase: GetLatestMoviesUseCase,
    // ... other use cases
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: HomeEvent) {
        // Handle UI events
    }
}
```

### 2. Result Wrapper
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 3. Repository Pattern
```kotlin
interface MovieRepository {
    fun getLatestMovies(): Flow<Result<List<Movie>>>
    fun getTopRatedMovies(): Flow<Result<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Result<Movie>>
    // ... other methods
}
```

### 4. UseCase Pattern
```kotlin
class GetLatestMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<Result<List<Movie>>> {
        return repository.getLatestMovies()
    }
}
```

## Navigation Routes
- `home` - Main screen with movie sections
- `movieDetail/{movieId}` - Movie details screen
- `seeAll/{category}` - Full list of movies by category
- `favorites` - Saved favorite movies

## UI Components
- **MovieCard**: Reusable component showing poster, title, rating, favorite button
- **HorizontalMovieList**: LazyRow with MovieCards
- **MovieGrid**: LazyVerticalGrid for "See All" screens

## API Integration
- Base URL placeholder: `YOUR_BASE_URL_HERE`
- Endpoints needed: latest movies, top rated movies, recommended movies, movie detail
- Use DTO models for API responses

## Database (Room)
- Store favorite movies locally
- `MovieEntity` with fields: id, title, posterPath, rating, releaseDate, overview
- DAO functions: `insertFavorite`, `deleteFavorite`, `getFavoriteMovies`, `isFavorite`

## When Adding New Features

### For New Screens:
1. Create screen package under `presentation/`
2. Define `UiState`, `UiEvent`, `UiEffect`
3. Create ViewModel with UseCase dependencies
4. Implement Compose UI with Material 3 components
5. Add navigation route

### For New API Endpoints:
1. Add to `ApiService` interface in `data/remote/`
2. Create DTO model in `data/remote/model/`
3. Add mapper function in `data/mapper/`
4. Update repository implementation
5. Create UseCase in `domain/usecase/`
6. Update ViewModel and UI

## Testing Guidelines
- Unit tests for UseCases, ViewModels, Repository
- Instrumentation tests for UI components
- Test ViewModel state transitions
- Test repository data flows

## Code Quality
- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add KDoc documentation for public APIs
- Handle edge cases and error states
- Implement proper loading states
- Use sealed classes for state management

## Dependencies Management
- Check `build.gradle.kts` files for current dependencies
- Keep dependencies updated to latest stable versions
- Use version catalogs if available

## Common Tasks
- Add new movie category: Follow "New API Endpoints" flow
- Add new UI feature: Extend existing ViewModel and UI
- Optimize performance: Consider Paging 3 for large lists
- Add analytics: Create Analytics service in `core/`

## Notes for AI Agents
- Always check existing code patterns before adding new code
- Follow the established architecture patterns
- Use dependency injection (Hilt) for all dependencies
- Maintain separation of concerns between layers
- Test changes thoroughly before considering them complete

## Project Status
Refer to `prompt.md` for the complete original project requirements and detailed specifications.
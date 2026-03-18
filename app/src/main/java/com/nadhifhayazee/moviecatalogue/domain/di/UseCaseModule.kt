package com.nadhifhayazee.moviecatalogue.domain.di

import com.nadhifhayazee.moviecatalogue.domain.usecase.AddFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetFavoriteMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetLatestMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetMovieDetailUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetRecommendedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.GetTopRatedMoviesUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.IsFavoriteMovieUseCase
import com.nadhifhayazee.moviecatalogue.domain.usecase.RemoveFavoriteMovieUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetLatestMoviesUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): GetLatestMoviesUseCase {
        return GetLatestMoviesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTopRatedMoviesUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): GetTopRatedMoviesUseCase {
        return GetTopRatedMoviesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRecommendedMoviesUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): GetRecommendedMoviesUseCase {
        return GetRecommendedMoviesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetMovieDetailUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): GetMovieDetailUseCase {
        return GetMovieDetailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFavoriteMoviesUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): GetFavoriteMoviesUseCase {
        return GetFavoriteMoviesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddFavoriteMovieUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): AddFavoriteMovieUseCase {
        return AddFavoriteMovieUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveFavoriteMovieUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): RemoveFavoriteMovieUseCase {
        return RemoveFavoriteMovieUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideIsFavoriteMovieUseCase(
        repository: com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
    ): IsFavoriteMovieUseCase {
        return IsFavoriteMovieUseCase(repository)
    }
}

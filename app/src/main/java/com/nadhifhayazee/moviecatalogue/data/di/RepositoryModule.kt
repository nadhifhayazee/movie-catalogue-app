package com.nadhifhayazee.moviecatalogue.data.di

import com.nadhifhayazee.moviecatalogue.data.local.LocalDataSource
import com.nadhifhayazee.moviecatalogue.data.remote.RemoteDataSource
import com.nadhifhayazee.moviecatalogue.data.repository.MovieRepositoryImpl
import com.nadhifhayazee.moviecatalogue.domain.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMovieRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource
    ): MovieRepository {
        return MovieRepositoryImpl(remoteDataSource, localDataSource)
    }
}

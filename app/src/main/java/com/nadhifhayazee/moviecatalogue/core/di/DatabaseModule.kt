package com.nadhifhayazee.moviecatalogue.core.di

import android.content.Context
import com.nadhifhayazee.moviecatalogue.data.local.database.MovieDatabase
import com.nadhifhayazee.moviecatalogue.data.local.database.MIGRATION_1_2
import com.nadhifhayazee.moviecatalogue.data.local.database.MIGRATION_2_3
import com.nadhifhayazee.moviecatalogue.data.local.dao.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(
        @ApplicationContext context: Context
    ): MovieDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            MovieDatabase::class.java,
            "movie_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         .build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }
}

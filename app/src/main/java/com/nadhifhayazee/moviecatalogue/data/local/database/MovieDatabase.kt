package com.nadhifhayazee.moviecatalogue.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nadhifhayazee.moviecatalogue.data.local.dao.MovieDao
import com.nadhifhayazee.moviecatalogue.data.local.entity.FavoriteMovieEntity
import com.nadhifhayazee.moviecatalogue.data.local.entity.CachedMovieEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `movies_new` (
                `id` INTEGER NOT NULL, 
                `title` TEXT NOT NULL, 
                `poster_path` TEXT, 
                `backdrop_path` TEXT, 
                `overview` TEXT NOT NULL, 
                `release_date` TEXT NOT NULL, 
                `vote_average` REAL NOT NULL, 
                `category` TEXT, 
                `cache_timestamp` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        
        database.execSQL("""
            INSERT INTO `movies_new` (`id`, `title`, `poster_path`, `backdrop_path`, `overview`, `release_date`, `vote_average`, `category`, `cache_timestamp`)
            SELECT `id`, `title`, `posterPath`, `backdropPath`, `overview`, `releaseDate`, `voteAverage`, NULL, 0
            FROM `movies`
        """.trimIndent())
        
        database.execSQL("DROP TABLE `movies`")
        database.execSQL("ALTER TABLE `movies_new` RENAME TO `movies`")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create favorite_movies table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `favorite_movies` (
                `id` INTEGER NOT NULL, 
                `title` TEXT NOT NULL, 
                `poster_path` TEXT, 
                `backdrop_path` TEXT, 
                `overview` TEXT NOT NULL, 
                `release_date` TEXT NOT NULL, 
                `vote_average` REAL NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        // Move favorites from movies table to favorite_movies
        // Assuming movies with category IS NULL were the favorites (based on previous migration)
        // Actually, in the old setup, 'movies' was used for both.
        // Let's migrate all existing movies that don't have a category or we can just migrate all if we want to preserve them as favorites.
        // Usually, cached movies are temporary, favorites are permanent.
        database.execSQL("""
            INSERT INTO `favorite_movies` (`id`, `title`, `poster_path`, `backdrop_path`, `overview`, `release_date`, `vote_average`)
            SELECT `id`, `title`, `poster_path`, `backdrop_path`, `overview`, `release_date`, `vote_average`
            FROM `movies`
            WHERE `category` IS NULL
        """.trimIndent())

        // Create cached_movies table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `cached_movies` (
                `id` INTEGER NOT NULL, 
                `title` TEXT NOT NULL, 
                `poster_path` TEXT, 
                `backdrop_path` TEXT, 
                `overview` TEXT NOT NULL, 
                `release_date` TEXT NOT NULL, 
                `vote_average` REAL NOT NULL, 
                `category` TEXT NOT NULL, 
                `cache_timestamp` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        // Move cached movies from movies table to cached_movies
        database.execSQL("""
            INSERT INTO `cached_movies` (`id`, `title`, `poster_path`, `backdrop_path`, `overview`, `release_date`, `vote_average`, `category`, `cache_timestamp`)
            SELECT `id`, `title`, `poster_path`, `backdrop_path`, `overview`, `release_date`, `vote_average`, `category`, `cache_timestamp`
            FROM `movies`
            WHERE `category` IS NOT NULL
        """.trimIndent())

        // Drop the old movies table
        database.execSQL("DROP TABLE `movies`")
    }
}

@Database(
    entities = [FavoriteMovieEntity::class, CachedMovieEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}

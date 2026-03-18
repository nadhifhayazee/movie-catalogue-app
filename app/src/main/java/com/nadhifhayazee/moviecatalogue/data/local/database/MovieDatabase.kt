package com.nadhifhayazee.moviecatalogue.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nadhifhayazee.moviecatalogue.data.local.dao.MovieDao
import com.nadhifhayazee.moviecatalogue.data.local.entity.MovieEntity

@Database(
    entities = [MovieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}

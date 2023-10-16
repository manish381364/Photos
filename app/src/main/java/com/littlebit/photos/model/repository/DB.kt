package com.littlebit.photos.model.repository

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.littlebit.photos.model.ThemePreference

@Dao
interface ThemePreferenceDao {
    @Query("SELECT * FROM theme_preference LIMIT 1")
    suspend fun getThemePreference(): ThemePreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(themePreference: ThemePreference)

}


@Database(entities = [ThemePreference::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themePreferenceDao(): ThemePreferenceDao
}

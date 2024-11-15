package com.iate.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.data.database.entity.Food

@Database(entities = [Food::class, UserSettings::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun userSettingsDao(): UserSettingsDao
}
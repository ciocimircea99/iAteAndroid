package com.iate.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iate.android.data.database.entity.CalorieGoals
import com.iate.android.data.database.entity.Food

@Database(entities = [Food::class, CalorieGoals::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun calorieGoalsDao(): CalorieGoalsDao
}
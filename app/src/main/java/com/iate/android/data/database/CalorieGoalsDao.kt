package com.iate.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iate.android.data.database.entity.CalorieGoals

@Dao
interface CalorieGoalsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUserCalorieGoals(calorieGoals: CalorieGoals)

    @Query("SELECT * FROM goals LIMIT 1")
    suspend fun getUserCalorieGoals(): CalorieGoals?

    @Query("DELETE FROM goals")
    suspend fun clearCalorieGoals()
}
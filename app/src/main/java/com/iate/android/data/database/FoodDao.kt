package com.iate.android.data.database

import androidx.room.*
import com.iate.android.data.database.entity.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFood(food: Food)

    @Query("SELECT * FROM foods")
    fun getFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE date = :date")
    fun getFoodsByDate(date: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE date BETWEEN :startDate AND :endDate")
    fun getFoodsByDateRange(startDate: String, endDate: String): Flow<List<Food>>

    @Delete
    suspend fun deleteFood(food: Food)

    @Query("DELETE FROM foods")
    suspend fun clearFoods()

}

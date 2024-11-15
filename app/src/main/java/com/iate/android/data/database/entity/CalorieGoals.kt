package com.iate.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class CalorieGoals(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val daily: Int,
    val weekly: Int,
    val monthly: Int
)
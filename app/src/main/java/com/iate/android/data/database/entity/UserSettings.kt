package com.iate.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val metric: Boolean,
    val age: Int,
    val gender: String,
    val height: Double, // Changed from Int to Double
    val weight: Double, // Changed from Int to Double
    val activityLevel: String,
    val bmr: Int,
    val tdee: Int,
)
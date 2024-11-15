package com.iate.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iate.android.data.database.entity.UserSettings

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUserSettings(userSettings: UserSettings)

    @Query("SELECT * FROM user_settings LIMIT 1")
    suspend fun getUserSettings(): UserSettings?

    @Query("DELETE FROM user_settings")
    suspend fun clearUserSettings()
}
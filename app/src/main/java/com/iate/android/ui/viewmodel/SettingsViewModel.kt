package com.iate.android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iate.android.data.database.UserSettingsDao
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.ui.base.BaseViewModel

class SettingsViewModel(
    private val userSettingsDao: UserSettingsDao
) : BaseViewModel() {

    private val _userSettings = MutableLiveData<UserSettings>()
    val userSettings: LiveData<UserSettings> = _userSettings

    private val activityLevels = mapOf(
        "Sedentary" to 1.2,
        "Light Exercise" to 1.375,
        "Moderate Exercise" to 1.55,
        "Active" to 1.725,
        "Very Active" to 1.9
    )

    init {
        fetchUserSettings()
    }

    private fun fetchUserSettings() = runCachingCoroutine {
        val userSettings = userSettingsDao.getUserSettings() ?: UserSettings(
            id = 1,
            metric = true,
            age = 30,
            gender = "Male",
            height = 170.0,
            weight = 70.0,
            activityLevel = "Sedentary",
            bmr = 0,
            tdee = 0
        )
        _userSettings.postValue(userSettings)
    }

    private fun calculateBMR(age: Int, gender: String, height: Double, weight: Double): Double {
        // Height in cm, weight in kg
        return if (gender == "Male") {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.33 * age)
        }
    }

    private fun calculateTDEE(bmr: Double, activityLevel: String): Double {
        return bmr * (activityLevels[activityLevel] ?: 1.2)
    }

    fun saveUserSettings(
        metric: Boolean,
        age: Int,
        gender: String,
        height: Double,
        weight: Double,
        activityLevel: String
    ) = runCachingCoroutine {
        val bmr = calculateBMR(age, gender, height, weight)
        val tdee = calculateTDEE(bmr, activityLevel)
        userSettingsDao.setUserSettings(
            UserSettings(
                id = 1,
                metric = metric,
                age = age,
                gender = gender,
                height = height,
                weight = weight,
                activityLevel = activityLevel,
                bmr = bmr.toInt(),
                tdee = tdee.toInt()
            )
        )
        postNavigationCommand(NAVIGATE_BACK)
    }
}

package com.iate.android.ui.viewmodel

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.UserSettingsDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.ui.base.BaseViewModel
import java.io.File

class MainViewModel(
    private val foodDao: FoodDao,
    private val openAIApi: OpenAIApi,
    private val userSettingsDao: UserSettingsDao,
) : BaseViewModel() {

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _foodList = MutableLiveData<List<Food>>()
    private val _userSettings = MutableLiveData<UserSettings>()

    val foodListAndUserSettings = MediatorLiveData<Pair<List<Food>, UserSettings>>()

    init {
        // Initialize MediatorLiveData to combine food list and user settings
        foodListAndUserSettings.addSource(_foodList) { value1 ->
            val value2 = _userSettings.value
            if (value2 != null) {
                foodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        foodListAndUserSettings.addSource(_userSettings) { value2 ->
            val value1 = _foodList.value
            if (value1 != null) {
                foodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        // Fetch initial user settings if necessary
        fetchUserSettings()
    }

    // Fetch user settings from the database
    private fun fetchUserSettings() = runCachingCoroutine {
        userSettingsDao.getUserSettings()?.let {
            _userSettings.postValue(it)
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        fetchFoodsForDate(date)
    }

    private fun fetchFoodsForDate(date: String) = runCachingCoroutine {
        val foods = foodDao.getFoodsByDate(date) // Synchronous query
        _foodList.postValue(foods)
    }

    fun addFood(foodDescription: String) = runCachingCoroutine {
        val response = openAIApi.getFoodFromText(foodDescription)
        val messageContent = response.choices.firstOrNull()?.message?.content?.trim()

        if (!messageContent.isNullOrEmpty()) {
            val lines = messageContent.split("\n")
            val name = lines[0].split(":")[1].trim()
            val calories = lines[1].split(":")[1].trim().split(" ")[0].toInt()
            val grams = lines[2].split(":")[1].trim().split(" ")[0].toInt()

            val food = Food(
                name = name,
                calories = calories,
                grams = grams,
                date = _selectedDate.value ?: ""
            )

            foodDao.addFood(food)
            // Refresh the food list after adding
            fetchFoodsForDate(_selectedDate.value ?: "")
        } else {
            _errorResult.postValue(Exception("Failed to parse API response"))
        }
    }

    fun addFoodFromImage(imageFile: File, onFoodAdded: () -> Unit) = runCachingCoroutine {
        val fileBytes = imageFile.readBytes()
        val base64Image = Base64.encodeToString(fileBytes, Base64.DEFAULT)
        val response = openAIApi.getFoodFromImage(base64Image)
        val messageContent = response.choices.firstOrNull()?.message?.content?.trim()

        if (!messageContent.isNullOrEmpty()) {
            val lines = messageContent.split("\n")
            val name = lines[0].split(":")[1].trim()
            val calories = lines[1].split(":")[1].trim().split(" ")[0].toInt()
            val grams = lines[2].split(":")[1].trim().split(" ")[0].toInt()

            val food = Food(
                name = name,
                calories = calories,
                grams = grams,
                date = _selectedDate.value ?: ""
            )

            foodDao.addFood(food)
            // Refresh the food list after adding
            fetchFoodsForDate(_selectedDate.value ?: "")

            onFoodAdded()
        } else {
            _errorResult.postValue(Exception("Failed to parse API response"))
        }
    }

    fun deleteFood(food: Food) = runCachingCoroutine {
        foodDao.deleteFood(food)
        fetchFoodsForDate(_selectedDate.value ?: "")
    }
}

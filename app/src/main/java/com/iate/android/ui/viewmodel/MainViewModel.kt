package com.iate.android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.UserSettingsDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.data.openai.model.ChatMessage
import com.iate.android.data.openai.model.CompletionRequest
import com.iate.android.ui.base.BaseViewModel

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
        val request = CompletionRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                ChatMessage(
                    role = "user",
                    content = """
                                You are a GPT model trained in nutrition. You know how much kcalories are in basic foods, 
                                and can also calculate an approximate number of calories per any meal. 
                                Given the food description "$foodDescription", take everything into account especially 
                                the formulation, kcal/g, what could the ingredients be, how many grams are in total, what should the kcal be / 100g then do the math.
                                Your response should only be in the following format:
                                Food Name: [meal name]
                                Calories: [calorie count] kcal
                                Grams: [weight in grams] g
                                Provide the meal name, its calorie content, and estimated weight in grams in the format above.
                            """.trimIndent()
                )
            ),
            max_tokens = 150,
            temperature = 0.5
        )

        val response = openAIApi.getCompletion(request)
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

    fun deleteFood(food: Food) = runCachingCoroutine {
        foodDao.deleteFood(food)
        fetchFoodsForDate(_selectedDate.value ?: "")
    }
}

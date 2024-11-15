package com.iate.android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.data.openai.model.ChatMessage
import com.iate.android.data.openai.model.CompletionRequest
import com.iate.android.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val foodDao: FoodDao,
    private val openAIApi: OpenAIApi
) : ViewModel() {

    private val _errorResult = SingleLiveEvent<Exception?>()
    val errorResult: MutableLiveData<Exception?> = _errorResult

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _foodList = MutableLiveData<List<Food>>()
    val foodList: LiveData<List<Food>> = _foodList

    fun setDate(date: String) {
        _selectedDate.value = date
        fetchFoodsForDate(date)
    }

    private fun fetchFoodsForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val foods = foodDao.getFoodsByDate(date) // Synchronous query
                _foodList.postValue(foods)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.postValue(e)
            }
        }
    }

    fun addFood(foodDescription: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.postValue(e)
            }
        }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                foodDao.deleteFood(food)
                // Refresh the food list after deletion
                fetchFoodsForDate(_selectedDate.value ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.postValue(e)
            }
        }
    }
}
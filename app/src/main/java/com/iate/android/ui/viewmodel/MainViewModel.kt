package com.iate.android.ui.viewmodel

import DateTimeUtil
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.data.openai.model.ChatMessage
import com.iate.android.data.openai.model.CompletionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Date

class MainViewModel(
    private val foodDao: FoodDao,
    private val openAIApi: OpenAIApi
) : ViewModel(), KoinComponent {

    private val _errorResult = MutableStateFlow<Exception?>(null)
    val errorResult: StateFlow<Exception?> = _errorResult

    private val _foodList = MutableStateFlow<List<Food>>(emptyList())
    val foodList: StateFlow<List<Food>> = _foodList

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate

    fun addFood(foodDescription: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prepare the request
                val request = CompletionRequest(
                    model = "gpt-4o-mini", // Replace with the correct model
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

                // Call the OpenAI API
                val response = openAIApi.getCompletion(request)

                // Parse the response
                val messageContent = response.choices.firstOrNull()?.message?.content?.trim()
                if (!messageContent.isNullOrEmpty()) {
                    val lines = messageContent.split("\n")
                    val name = lines[0].split(":")[1].trim()
                    val calories = lines[1].split(":")[1].trim().split(" ")[0].toInt()
                    val grams = lines[2].split(":")[1].trim().split(" ")[0].toInt()

                    // Create a Food object
                    val food = Food(
                        name = name,
                        calories = calories,
                        grams = grams,
                        date = _selectedDate.value // Use appropriate date format
                    )

                    // Save to the database
                    foodDao.addFood(food)

                    // No need to emit success here as the list will be updated by `fetchFoods()`
                } else {
                    _errorResult.emit(Exception("Failed to parse API response"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.emit(e)
            }
        }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                foodDao.deleteFood(food)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.emit(e)
            }
        }
    }

    private fun fetchAllFoods(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                foodDao.getFoodsByDate(date).collectLatest { foods ->
                    _foodList.emit(foods) // Emit the list of foods
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.emit(e)
            }
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        fetchAllFoods(date)
    }
}

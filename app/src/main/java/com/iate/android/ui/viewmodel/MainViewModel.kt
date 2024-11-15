package com.iate.android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.data.openai.model.ChatMessage
import com.iate.android.data.openai.model.CompletionRequest
import com.iate.android.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class MainViewModel(
    private val foodDao: FoodDao,
    private val openAIApi: OpenAIApi
) : BaseViewModel(), KoinComponent {

    private val _foodResult = MutableStateFlow<Result<Food>?>(null)
    val foodResult: StateFlow<Result<Food>?> = _foodResult

    init {
        Log.d("MainViewModel", "FoodDao instance: $foodDao")
    }

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
                        date = System.currentTimeMillis().toString() // Use appropriate date format
                    )

                    // Save to the database
                    foodDao.addFood(food)

                    // Emit the result
                    _foodResult.emit(Result.success(food))
                } else {
                    _foodResult.emit(Result.failure(Exception("Failed to parse API response")))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _foodResult.emit(Result.failure(e))
            }
        }
    }
}
package com.iate.android.data.openai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.iate.android.BuildConfig
import kotlin.time.Duration.Companion.seconds

class OpenAIApi {

    private val openAi = OpenAI(
        token = BuildConfig.OPENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
    )

    suspend fun getFoodFromText(input: String): ChatCompletion {


        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-4o"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = """
                                You are a GPT model trained in nutrition. You know how much kcalories are in basic foods, 
                                and can also calculate an approximate number of calories per any meal from text input or pictures. 
                                You search the web for the best nutritional databases and you use them in order to make your response 
                                as accurate as possible. You can analyze food descriptions and pictures to extract the relevant nutritional
                                data from.
                                Analyze the food description: $input. 
                                Your task is to extract:
                                1. The name of the meal.
                                2. Compute the calorie content (kcal) of the meal as accurately as possible.
                                3. Approximate the weight of the food (grams) as accurately as possible.
                                
                                Respond only with a valid JSON object in this exact format:
                                
                                {
                                    "foodName": "example",
                                    "foodCalories": 0,
                                    "foodWeight": 0
                                }
                                
                                If the nutritional values cannot be determined, respond with:
                                
                                {
                                    "foodName": "Unknown",
                                    "foodCalories": 0,
                                    "foodWeight": 0
                                }
                                
                                Do not include any Markdown formatting like ``` or any explanatory text. The response must be raw JSON, nothing else.
                            """.trimIndent()
                ),
            )
        )
        return openAi.chatCompletion(chatCompletionRequest)
    }

    suspend fun getFoodFromImage(base64Image: String): ChatCompletion {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-4o"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        TextPart(
                            text = """
                                You are a GPT model trained in nutrition. You know how much kcalories are in basic foods, 
                                and can also calculate an approximate number of calories per any meal from text input or pictures. 
                                You search the web for the best nutritional databases and you use them in order to make your response 
                                as accurate as possible. You can analyze food descriptions and pictures to extract the relevant nutritional
                                data from.
                                Analyze the image attached. 
                                Your task is to extract:
                                1. The name of the meal.
                                2. Compute the calorie content (kcal) of the meal as accurately as possible.
                                3. Approximate the weight of the food (grams) as accurately as possible.
                                
                                Respond only with a valid JSON object in this exact format:
                                
                                {
                                    "foodName": "example",
                                    "foodCalories": 0,
                                    "foodWeight": 0
                                }
                                
                                If the nutritional values cannot be determined, respond with:
                                
                                {
                                    "foodName": "Unknown",
                                    "foodCalories": 0,
                                    "foodWeight": 0
                                }
                                
                                Do not include any Markdown formatting like ``` or any explanatory text. The response must be raw JSON, nothing else.
                            """.trimIndent()
                        ),
                        ImagePart(
                            imageUrl = ImagePart.ImageURL(url = "data:image/jpeg;base64,${base64Image}")
                        )
                    )
                ),
            )
        )
        return openAi.chatCompletion(chatCompletionRequest)
    }
}
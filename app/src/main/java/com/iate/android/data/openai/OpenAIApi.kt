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
            model = ModelId("gpt-4o-mini"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = """
                                You are a GPT model trained in nutrition. You know how much kcalories are in basic foods, 
                                and can also calculate an approximate number of calories per any meal. 
                                Given the food description "$input", take everything into account especially 
                                the formulation, kcal/g, what could the ingredients be, how many grams are in total, what should the kcal be / 100g then do the math.
                                Your response should only be in the following format:
                                Food Name: [meal name]
                                Calories: [calorie count] kcal
                                Grams: [weight in grams] g
                                Provide the meal name, its calorie content, and estimated weight in grams in the format above do not use dots or commas in your answer.
                            """.trimIndent()
                ),
            )
        )
        return openAi.chatCompletion(chatCompletionRequest)
    }

    suspend fun getFoodFromImage(base64Image:String): ChatCompletion {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-4o-mini"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        TextPart(
                            text = """
                                You are a GPT model trained in nutrition. You know how much kcalories are in basic foods, 
                                and can also calculate an approximate number of calories per any meal. 
                                Given the food inside the picture attached to this message, take everything into account especially 
                                the formulation, kcal/g, what could the ingredients be, how many grams are in total, what should the kcal be / 100g then do the math.
                                Your response should only be in the following format:
                                Food Name: [meal name]
                                Calories: [calorie count] kcal
                                Grams: [weight in grams] g
                                Provide the meal name, its calorie content, and estimated weight in grams in the format above do not use dots or commas in your answer.
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
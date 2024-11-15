package com.iate.android.data.openai

import com.iate.android.data.openai.model.CompletionRequest
import com.iate.android.data.openai.model.CompletionResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Body request: CompletionRequest
    ): CompletionResponse
}
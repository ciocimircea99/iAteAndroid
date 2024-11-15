package com.iate.android.data.openai.model

data class CompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int = 100,
    val temperature: Double = 0.7
)
package com.iate.android.data.openai.model

data class Choice(
    val message: ChatMessage,
    val index: Int,
    val finish_reason: String
)
package com.iate.android.data.openai.model

data class CompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long
)
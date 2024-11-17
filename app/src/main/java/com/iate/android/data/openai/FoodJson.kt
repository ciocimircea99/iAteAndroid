package com.iate.android.data.openai

import kotlinx.serialization.Serializable

@Serializable
data class FoodJson(
    val foodName : String? = null,
    val foodCalories : Int? = null,
    val foodWeight : Int? = null
)
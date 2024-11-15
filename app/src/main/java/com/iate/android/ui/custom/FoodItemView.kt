package com.iate.android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.iate.android.data.database.entity.Food
import com.iate.android.databinding.ViewFoodItemBinding

class FoodItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewFoodItemBinding =
        ViewFoodItemBinding.inflate(LayoutInflater.from(context), this, true)

    fun setupItemView(food: Food, onDeleteClicked: (food: Food) -> Unit) {
        binding.foodName.text = food.name.toString()
        binding.foodCalories.text = food.calories.toString()
        binding.foodGrams.text = food.grams.toString()
        binding.buttonDelete.setOnClickListener { onDeleteClicked(food) }
    }
}
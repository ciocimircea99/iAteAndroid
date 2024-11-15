package com.iate.android

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.iate.android.databinding.ActivityMainBinding
import com.iate.android.ui.base.BaseActivity
import com.iate.android.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(ActivityMainBinding::inflate) {

    override val viewModel: MainViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use `binding` and `viewModel` here

        // Example food description
        val foodDescription = "A bowl of rice with chicken and vegetables"

        // Observe the food result
        lifecycleScope.launch {
            viewModel.foodResult.collectLatest { result ->
                result?.onSuccess { food ->
                    // Display success message
                    Toast.makeText(this@MainActivity, "Added Food: ${food.name}", Toast.LENGTH_SHORT).show()
                }?.onFailure { error ->
                    // Display error message
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Call addFood
        viewModel.addFood(foodDescription)
    }
}

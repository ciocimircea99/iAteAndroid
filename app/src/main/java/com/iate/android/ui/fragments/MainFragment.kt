package com.iate.android.ui.fragments

import DateTimeUtil
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.iate.android.databinding.FragmentMainBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.custom.DateSelectorView
import com.iate.android.ui.custom.FoodItemView
import com.iate.android.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date

class MainFragment :
    BaseFragment<FragmentMainBinding, MainViewModel>(FragmentMainBinding::inflate) {

    override val viewModel: MainViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

        // Observe food list updates
        lifecycleScope.launch {
            viewModel.foodList.collectLatest { foodList ->
                binding.foodContainer.removeAllViews()
                foodList.forEach { food ->
                    binding.foodContainer.addView(
                        FoodItemView(requireContext()).apply {
                            setupItemView(food) { food ->
                                viewModel.deleteFood(food)
                            }
                        }
                    )
                }
            }
        }

        // Observe errors
        lifecycleScope.launch {
            viewModel.errorResult.collectLatest { error ->
                error?.let {
                    // Display error message in case of an error
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            viewModel.addFood(binding.foodDescription.text.toString())
        }

        binding.dateSelector.dateSelectedListener = object : DateSelectorView.OnDateSelectedListener {
            override fun onDateSelected(date: String) {
                viewModel.setDate(date)
            }
        }

        viewModel.setDate(DateTimeUtil.currentTimeMillisToIso8601())
    }
}
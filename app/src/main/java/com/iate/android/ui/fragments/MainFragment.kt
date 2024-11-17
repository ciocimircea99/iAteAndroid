package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iate.android.R
import com.iate.android.databinding.FragmentMainBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.base.TakePictureFragment
import com.iate.android.ui.custom.DateSelectorView
import com.iate.android.ui.custom.FoodItemView
import com.iate.android.ui.viewmodel.MainViewModel
import com.iate.android.util.DateTimeUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.math.abs

class MainFragment : TakePictureFragment<FragmentMainBinding, MainViewModel>(FragmentMainBinding::inflate) {

    override val viewModel: MainViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

        // Observe food list updates
        lifecycleScope.launch {
            viewModel.foodListAndUserSettings.observe(viewLifecycleOwner) { foodListAndUserSettings ->
                val foodList = foodListAndUserSettings.first
                val userSettings = foodListAndUserSettings.second

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

                val totalCalories = foodList.sumOf { it.calories }
                val calorieDeficit = userSettings.tdee - totalCalories
                val weightChange = abs(calorieDeficit) / 7700.0

                binding.summaryContainer.calories.text = totalCalories.toString()
                binding.summaryContainer.bmr.text = userSettings.tdee.toString()
                binding.summaryContainer.calorieDeficit.text = (calorieDeficit).toString()
                binding.summaryContainer.weightChange.text = String.format("%.2f kg", weightChange)

                if (calorieDeficit < 0) {
                    binding.summaryContainer.labelWeightChange.text = getString(R.string.estimated_weight_gained)
                } else {
                    binding.summaryContainer.labelWeightChange.text = getString(R.string.estimated_weight_lost)
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            viewModel.addFood(binding.foodDescription.text.toString())
        }

        binding.buttonPicture.setOnClickListener {
            startTakingPicture()
        }

        binding.buttonHistory.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_main_to_historyFragment)
        }

        binding.buttonSettings.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_main_to_settingsFragment)
        }

        binding.dateSelector.dateSelectedListener =
            object : DateSelectorView.OnDateSelectedListener {
                override fun onDateSelected(date: String) {
                    viewModel.setDate(date)
                }
            }

        viewModel.setDate(DateTimeUtil.currentTimeMillisToIso8601())
    }

    override fun onCameraPermissionDenied() {
        toast(getString(R.string.camera_permission_denied_please_give_your_permission_inside_settings))
    }

    override fun onPictureTaken(imageFile: File) {
        viewModel.addFoodFromImage(imageFile) {
            deleteImageFile()
        }
    }

    override fun onPictureFailed() {
        toast(getString(R.string.failed_to_load_picture))
    }
}
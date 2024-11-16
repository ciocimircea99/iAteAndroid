package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iate.android.R
import com.iate.android.databinding.FragmentSettingsBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModel()

    private var isMetric: Boolean = true  // To keep track of the current unit system

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.userSettings.observe(viewLifecycleOwner) { userSettings ->
                // Set the switch state and units
                isMetric = userSettings.metric
                binding.switchMetric.isChecked = isMetric

                // Update labels based on unit system
                updateUnitLabels()

                // Set the input fields with converted values
                val height = userSettings.height.toDouble()
                val weight = userSettings.weight.toDouble()

                if (isMetric) {
                    binding.heightInput.setText(height.toInt().toString())
                    binding.weightInput.setText(weight.toInt().toString())
                } else {
                    // Convert from metric to imperial
                    val heightInInches = cmToInches(height)
                    val weightInPounds = kgToPounds(weight)
                    binding.heightInput.setText(heightInInches.toInt().toString())
                    binding.weightInput.setText(weightInPounds.toInt().toString())
                }

                binding.ageInput.setText(userSettings.age.toString())
                binding.genderSpinner.setSelection(
                    resources.getStringArray(R.array.gender_array).indexOf(userSettings.gender)
                )
                binding.activityLevelSpinner.setSelection(
                    resources.getStringArray(R.array.activity_levels).indexOf(userSettings.activityLevel)
                )
            }
        }

        // Add listener to the metric switch
        binding.switchMetric.setOnCheckedChangeListener { _, isChecked ->
            isMetric = isChecked
            // Convert height and weight values when the toggle is changed
            convertHeightAndWeight()
            // Update labels to reflect the new unit system
            updateUnitLabels()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSave.setOnClickListener {
            // Get the input values and convert to metric if needed
            val age = binding.ageInput.text.toString().toIntOrNull() ?: 0
            val gender = binding.genderSpinner.selectedItem.toString()
            val activityLevel = binding.activityLevelSpinner.selectedItem.toString()

            val heightInput = binding.heightInput.text.toString().toDoubleOrNull() ?: 0.0
            val weightInput = binding.weightInput.text.toString().toDoubleOrNull() ?: 0.0

            val heightInCm: Int
            val weightInKg: Int

            if (isMetric) {
                heightInCm = heightInput.toInt()
                weightInKg = weightInput.toInt()
            } else {
                // Convert from imperial to metric before saving
                heightInCm = inchesToCm(heightInput).toInt()
                weightInKg = poundsToKg(weightInput).toInt()
            }

            viewModel.saveUserSettings(
                isMetric,
                age,
                gender,
                heightInCm,
                weightInKg,
                activityLevel
            )
        }
    }

    private fun convertHeightAndWeight() {
        val heightText = binding.heightInput.text.toString()
        val weightText = binding.weightInput.text.toString()

        val heightValue = heightText.toDoubleOrNull()
        val weightValue = weightText.toDoubleOrNull()

        if (heightValue != null && weightValue != null) {
            if (isMetric) {
                // Convert from imperial to metric
                val heightInCm = inchesToCm(heightValue)
                val weightInKg = poundsToKg(weightValue)
                binding.heightInput.setText(heightInCm.toInt().toString())
                binding.weightInput.setText(weightInKg.toInt().toString())
            } else {
                // Convert from metric to imperial
                val heightInInches = cmToInches(heightValue)
                val weightInPounds = kgToPounds(weightValue)
                binding.heightInput.setText(heightInInches.toInt().toString())
                binding.weightInput.setText(weightInPounds.toInt().toString())
            }
        }
    }

    private fun updateUnitLabels() {
        if (isMetric) {
            binding.heightLabel.text = getString(R.string.height_cm)
            binding.weightLabel.text = getString(R.string.weight_kg)
        } else {
            binding.heightLabel.text = getString(R.string.height_in)
            binding.weightLabel.text = getString(R.string.weight_lb)
        }
    }

    // Conversion helper functions
    private fun cmToInches(cm: Double): Double = cm / 2.54
    private fun inchesToCm(inches: Double): Double = inches * 2.54
    private fun kgToPounds(kg: Double): Double = kg * 2.20462
    private fun poundsToKg(pounds: Double): Double = pounds / 2.20462
}

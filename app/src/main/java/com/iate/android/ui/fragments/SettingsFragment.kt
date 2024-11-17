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
import kotlin.math.roundToInt

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModel()

    private var isMetric: Boolean = true  // To keep track of the current unit system
    private var originalHeightInCm: Double = 0.0  // Store original height in cm
    private var originalWeightInKg: Double = 0.0  // Store original weight in kg

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.userSettings.observe(viewLifecycleOwner) { userSettings ->
                // Set the switch state and units
                isMetric = userSettings.metric
                binding.switchMetric.isChecked = isMetric

                // Store the original values
                originalHeightInCm = userSettings.height
                originalWeightInKg = userSettings.weight

                // Update labels based on unit system
                updateUnitLabels()

                // Set the input fields with appropriate values
                updateHeightAndWeightInputs()

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
            // Update labels to reflect the new unit system
            updateUnitLabels()
            // Update the input fields with converted values
            updateHeightAndWeightInputs()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSave.setOnClickListener {
            // Get the input values directly from the input fields
            val age = binding.ageInput.text.toString().toIntOrNull() ?: 0
            val gender = binding.genderSpinner.selectedItem.toString()
            val activityLevel = binding.activityLevelSpinner.selectedItem.toString()

            val heightText = binding.heightInput.text.toString()
            val weightText = binding.weightInput.text.toString()

            val heightValue = heightText.toDoubleOrNull()
            val weightValue = weightText.toDoubleOrNull()

            if (heightValue == null || weightValue == null) {
                Toast.makeText(requireContext(), "Please enter valid height and weight values.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightInCm: Double
            val weightInKg: Double

            if (isMetric) {
                heightInCm = heightValue
                weightInKg = weightValue
            } else {
                // Convert from imperial to metric before saving
                heightInCm = inchesToCm(heightValue)
                weightInKg = poundsToKg(weightValue)
            }

            // Update the original values for consistency
            originalHeightInCm = heightInCm
            originalWeightInKg = weightInKg

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

    private fun updateHeightAndWeightInputs() {
        if (isMetric) {
            binding.heightInput.setText(String.format("%.2f", originalHeightInCm))
            binding.weightInput.setText(String.format("%.2f", originalWeightInKg))
        } else {
            // Convert from metric to imperial
            val heightInInches = cmToInches(originalHeightInCm)
            val weightInPounds = kgToPounds(originalWeightInKg)
            binding.heightInput.setText(String.format("%.2f", heightInInches))
            binding.weightInput.setText(String.format("%.2f", weightInPounds))
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

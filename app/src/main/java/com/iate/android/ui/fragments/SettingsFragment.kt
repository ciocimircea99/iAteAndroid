package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iate.android.R
import com.iate.android.databinding.FragmentSettingsBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.custom.FoodItemView
import com.iate.android.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

        lifecycleScope.launch {
            viewModel.userSettings.observe(viewLifecycleOwner) { userSettings ->
                binding.switchMetric.isChecked = userSettings.metric
                binding.ageInput.setText(userSettings.age.toString())
                binding.genderSpinner.setSelection(
                    getString(R.string.gender).indexOf(
                        userSettings.gender
                    )
                )
                binding.heightInput.setText(userSettings.height.toString())
                binding.weightInput.setText(userSettings.weight.toString())
                binding.activityLevelSpinner.setSelection(
                    getString(R.string.activity_level).indexOf(
                        userSettings.activityLevel
                    )
                )
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSave.setOnClickListener {
            viewModel.saveUserSettings(
                binding.switchMetric.isChecked,
                binding.ageInput.text.toString().toInt(),
                binding.genderSpinner.selectedItem.toString(),
                binding.heightInput.text.toString().toInt(),
                binding.weightInput.text.toString().toInt(),
                binding.activityLevelSpinner.selectedItem.toString(),
            )
        }
    }
}
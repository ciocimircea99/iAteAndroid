package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import com.iate.android.databinding.FragmentSettingsBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.SettingsViewModel

class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    override fun getViewModelClass() = SettingsViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

    }
}
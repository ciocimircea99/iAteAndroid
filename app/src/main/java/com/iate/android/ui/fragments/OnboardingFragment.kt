package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import com.iate.android.databinding.FragmentOnboardingBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.OnboardingViewModel

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding, OnboardingViewModel>(FragmentOnboardingBinding::inflate) {

    override fun getViewModelClass() = OnboardingViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

    }
}
package com.iate.android.ui.fragments


import android.os.Bundle
import android.view.View
import com.iate.android.databinding.FragmentOnboardingBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding, OnboardingViewModel>(FragmentOnboardingBinding::inflate) {

    override val viewModel: OnboardingViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

    }
}
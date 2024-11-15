package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import com.iate.android.databinding.FragmentMainBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.MainViewModel

class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>(FragmentMainBinding::inflate) {

    override fun getViewModelClass() = MainViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

    }
}
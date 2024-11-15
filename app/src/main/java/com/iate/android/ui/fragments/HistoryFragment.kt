package com.iate.android.ui.fragments

import android.os.Bundle
import android.view.View
import com.iate.android.databinding.FragmentHistoryBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.HistoryViewModel

class HistoryFragment : BaseFragment<FragmentHistoryBinding, HistoryViewModel>(FragmentHistoryBinding::inflate) {

    override fun getViewModelClass() = HistoryViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

    }
}
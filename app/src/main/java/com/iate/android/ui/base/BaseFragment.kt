package com.iate.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding, VM : ViewModel>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> B
) : Fragment() {

    // ViewModel instance initialized lazily
    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[getViewModelClass()]
    }

    // ViewBinding instance
    private var _binding: B? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }

    // Abstract method to provide the ViewModel class type
    protected abstract fun getViewModelClass(): Class<VM>
}

package com.iate.android.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding, VM : ViewModel>(
    private val inflateBinding: (android.view.LayoutInflater) -> B
) : AppCompatActivity() {

    // ViewModel instance initialized lazily
    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[getViewModelClass()]
    }

    // ViewBinding instance
    private var _binding: B? = null
    protected val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // Prevent memory leaks
    }

    // Abstract method to provide the ViewModel class type
    protected abstract fun getViewModelClass(): Class<VM>
}

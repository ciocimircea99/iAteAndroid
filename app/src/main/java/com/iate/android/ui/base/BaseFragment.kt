package com.iate.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding, VM : ViewModel>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> B
) : Fragment() {

    // Use Koin to inject ViewModel
    protected abstract val viewModel: VM

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
        _binding = null // Prevent memory leaks
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (viewModel as? BaseViewModel)?.run {
            errorResult.observe(viewLifecycleOwner) { error ->
                toast(error?.message.toString())
            }
            navigationCommand.observe(viewLifecycleOwner) { actionID ->
                if (BaseViewModel.NAVIGATE_BACK == actionID) {
                    findNavController().popBackStack()
                } else {
                    findNavController().navigate(actionID)
                }
            }
        }
    }

    protected fun toast(message: String) {
        Toast.makeText(requireContext(), "Error: ${message}", Toast.LENGTH_LONG).show()
    }
}

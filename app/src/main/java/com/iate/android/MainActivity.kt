package com.iate.android

import android.os.Bundle
import com.iate.android.databinding.ActivityMainBinding
import com.iate.android.ui.base.BaseActivity
import com.iate.android.ui.viewmodel.MainViewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(ActivityMainBinding::inflate) {

    override fun getViewModelClass() = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use `binding` and `viewModel` here
    }
}

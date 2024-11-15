package com.iate.android

import android.os.Bundle
import com.iate.android.databinding.ActivityMainBinding
import com.iate.android.ui.base.BaseActivity
import com.iate.android.ui.base.BaseViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>(ActivityMainBinding::inflate) {

    override val viewModel: BaseViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

package com.iate.android.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iate.android.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    companion object {
        const val NAVIGATE_BACK = 1
    }

    protected val _errorResult = SingleLiveEvent<Exception?>()
    val errorResult: SingleLiveEvent<Exception?> = _errorResult

    private val _navigationCommand = SingleLiveEvent<Int>()
    val navigationCommand: MutableLiveData<Int> = _navigationCommand

    fun runCachingCoroutine(function: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                function.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorResult.postValue(e)
            }
        }
    }

    fun postNavigationCommand(actionID: Int) {
        _navigationCommand.postValue(actionID)
    }
}
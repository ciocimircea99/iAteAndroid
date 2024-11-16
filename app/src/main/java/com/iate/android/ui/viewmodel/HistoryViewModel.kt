package com.iate.android.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.iate.android.data.database.FoodDao
import com.iate.android.data.database.UserSettingsDao
import com.iate.android.data.database.entity.Food
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.ui.base.BaseViewModel
import com.iate.android.util.DateTimeUtil

class HistoryViewModel(
    private val foodDao: FoodDao,
    private val userSettingsDao: UserSettingsDao
) : BaseViewModel() {

    private val _dailyFoodList = MutableLiveData<List<Food>>()
    private val _weeklyFoodList = MutableLiveData<List<Food>>()
    private val _monthlyFoodList = MutableLiveData<List<Food>>()
    private val _yearlyFoodList = MutableLiveData<List<Food>>()
    private val _userSettings = MutableLiveData<UserSettings>()

    val dailyFoodListAndUserSettings = MediatorLiveData<Pair<List<Food>, UserSettings>>()
    val weeklyFoodListAndUserSettings = MediatorLiveData<Pair<List<Food>, UserSettings>>()
    val monthlyFoodListAndUserSettings = MediatorLiveData<Pair<List<Food>, UserSettings>>()
    val yearlyFoodListAndUserSettings = MediatorLiveData<Pair<List<Food>, UserSettings>>()

    init {
        // Initialize MediatorLiveData to combine food list and user settings for each period
        dailyFoodListAndUserSettings.addSource(_dailyFoodList) { value1 ->
            val value2 = _userSettings.value
            if (value2 != null) {
                dailyFoodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        weeklyFoodListAndUserSettings.addSource(_weeklyFoodList) { value1 ->
            val value2 = _userSettings.value
            if (value2 != null) {
                weeklyFoodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        monthlyFoodListAndUserSettings.addSource(_monthlyFoodList) { value1 ->
            val value2 = _userSettings.value
            if (value2 != null) {
                monthlyFoodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        yearlyFoodListAndUserSettings.addSource(_yearlyFoodList) { value1 ->
            val value2 = _userSettings.value
            if (value2 != null) {
                yearlyFoodListAndUserSettings.value = Pair(value1, value2)
            }
        }

        // Fetch initial user settings if necessary
        fetchUserSettings()
    }

    // Fetch user settings from the database
    private fun fetchUserSettings() = runCachingCoroutine {
        userSettingsDao.getUserSettings()?.let {
            // Post the user settings first
            _userSettings.postValue(it)

            // Fetch the daily food list only after user settings are updated
            fetchFoodsForDate(DateTimeUtil.currentTimeMillisToIso8601())
            fetchFoodsForWeek()
            fetchFoodsForMonth()
            fetchFoodsForYear()
        }
    }

    // Fetch foods for a specific date
    private fun fetchFoodsForDate(date: String) = runCachingCoroutine {
        val foods = foodDao.getFoodsByDate(date) // Synchronous query for daily foods
        _dailyFoodList.postValue(foods) // Post the food list to trigger the MediatorLiveData
    }

    // Fetch foods for the current week
    private fun fetchFoodsForWeek() = runCachingCoroutine {
        val dates = DateTimeUtil.getWeeklyChartDates() // Get weekly date range
        val foods = foodDao.getFoodsByDateRange(dates.first, dates.second) // Synchronous query for weekly foods

        _weeklyFoodList.postValue(foods) // Post the fetched foods to update MediatorLiveData
    }

    // Fetch foods for the current month
    private fun fetchFoodsForMonth() = runCachingCoroutine {
        val dates = DateTimeUtil.getMonthlyChartDates() // Get monthly date range
        val foods = foodDao.getFoodsByDateRange(dates.first, dates.second) // Synchronous query for monthly foods
        _monthlyFoodList.postValue(foods)
    }

    // Fetch foods for the current year
    private fun fetchFoodsForYear() = runCachingCoroutine {
        val dates = DateTimeUtil.getAnnualChartDates() // Get annual date range
        val foods = foodDao.getFoodsByDateRange(dates.first, dates.second) // Synchronous query for yearly foods
        _yearlyFoodList.postValue(foods)
    }
}

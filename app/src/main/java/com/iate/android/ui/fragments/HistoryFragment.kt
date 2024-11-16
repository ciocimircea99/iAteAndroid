package com.iate.android.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.iate.android.R
import com.iate.android.data.database.entity.Food
import com.iate.android.data.database.entity.UserSettings
import com.iate.android.databinding.FragmentHistoryBinding
import com.iate.android.ui.base.BaseFragment
import com.iate.android.ui.viewmodel.HistoryViewModel
import com.iate.android.util.ResourcesUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class HistoryFragment :
    BaseFragment<FragmentHistoryBinding, HistoryViewModel>(FragmentHistoryBinding::inflate) {

    override val viewModel: HistoryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use `binding` and `viewModel`

        lifecycleScope.launch {
            viewModel.dailyFoodListAndUserSettings.observe(viewLifecycleOwner) { foodListAndUserSettings ->
                val foodList = foodListAndUserSettings.first
                val userSettings = foodListAndUserSettings.second

                val totalCalories = foodList.sumOf { it.calories }
                val calorieDeficit = userSettings.tdee - totalCalories

                binding.dailySummary.calories.text = totalCalories.toString()
                binding.dailySummary.bmr.text = userSettings.tdee.toString()
                binding.dailySummary.calorieDeficit.text = (calorieDeficit).toString()
                binding.dailySummary.weightChange.text = (abs(calorieDeficit) / 7700.0).toString()

                if (calorieDeficit < 0) {
                    binding.dailySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_gained)
                } else {
                    binding.dailySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_lost)
                }

                initDailyChart(totalCalories, userSettings.tdee)
            }
            viewModel.weeklyFoodListAndUserSettings.observe(viewLifecycleOwner) { foodListAndUserSettings ->
                val foodList = foodListAndUserSettings.first  // List of foods consumed during the week
                val userSettings = foodListAndUserSettings.second  // User settings (including TDEE)

                // Get the current week and calculate the number of days in the week (7 days)
                val today = LocalDate.now(ZoneId.systemDefault())  // Get the current date
                val daysInWeek = 7  // Always 7 days in a week

                // Create an array to track total calories for each day of the week
                val dailyCalories = IntArray(daysInWeek)  // Array to store calories for each day (0 to 6)

                // Group food items by day of the week and sum the calories for each day
                for (food in foodList) {
                    val dayOfWeek = getDayOfWeek(food.date)  // Get the day of the week (0 = Sunday, 6 = Saturday)
                    if (dayOfWeek in 0 until daysInWeek) {
                        dailyCalories[dayOfWeek] += food.calories  // Add the food's calories to the appropriate day
                    }
                }

                // Calculate total calories for the week, including days with no food entries (assuming TDEE for those days)
                var totalCalories = 0
                for (i in 0 until daysInWeek) {
                    totalCalories += if (dailyCalories[i] == 0) userSettings.tdee else dailyCalories[i]
                }

                // Estimate the calorie deficit for the week
                val weeklyBmr = userSettings.tdee * daysInWeek  // Total TDEE for the week (TDEE * 7 days)
                val calorieDeficit = weeklyBmr - totalCalories  // Calorie deficit (or surplus)

                // Update the weekly summary text views
                binding.weeklySummary.calories.text = totalCalories.toString()  // Total calories consumed in the week
                binding.weeklySummary.bmr.text = weeklyBmr.toString()  // Total weekly BMR (TDEE * 7)
                binding.weeklySummary.calorieDeficit.text = calorieDeficit.toString()
                binding.weeklySummary.weightChange.text = (abs(calorieDeficit) / 7700.0).toString()

                // Display estimated weight change (loss or gain)
                if (calorieDeficit < 0) {
                    binding.weeklySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_gained)
                } else {
                    binding.weeklySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_lost)
                }

                // Initialize the weekly chart with food data and TDEE
                initWeeklyChart(foodList, userSettings.tdee)
            }
            viewModel.monthlyFoodListAndUserSettings.observe(viewLifecycleOwner) { foodListAndUserSettings ->
                val foodList = foodListAndUserSettings.first  // List of foods consumed during the month
                val userSettings = foodListAndUserSettings.second  // User settings (including TDEE)

                // Get the current month and calculate the number of days in the month
                val today = LocalDate.now(ZoneId.systemDefault())  // Get the current date
                val daysInMonth = today.lengthOfMonth()  // Get the number of days in the current month (28, 29, 30, or 31)

                // Create an array to track total calories for each day of the month
                val dailyCalories = IntArray(daysInMonth)

                // Group food items by day of the month and sum the calories for each day
                for (food in foodList) {
                    val dayOfMonth = getDayOfMonth(food.date)  // Get the day of the month (1 to 31)
                    if (dayOfMonth in 1..daysInMonth) {
                        dailyCalories[dayOfMonth - 1] += food.calories  // Add the food's calories to the appropriate day
                    }
                }

                // Calculate total calories for the month, including days with no food entries (assuming TDEE for those days)
                var totalCalories = 0
                for (i in 0 until daysInMonth) {
                    totalCalories += if (dailyCalories[i] == 0) userSettings.tdee else dailyCalories[i]
                }

                // Estimate the calorie deficit for the month
                val monthlyBmr = userSettings.tdee * daysInMonth  // Total TDEE for the month (TDEE * days in month)
                val calorieDeficit = monthlyBmr - totalCalories  // Calorie deficit (or surplus)

                // Update the monthly summary text views
                binding.monthlySummary.calories.text = totalCalories.toString()  // Total calories consumed in the month
                binding.monthlySummary.bmr.text = monthlyBmr.toString()  // Total monthly BMR (TDEE * days in month)
                binding.monthlySummary.calorieDeficit.text = calorieDeficit.toString()
                binding.monthlySummary.weightChange.text = (abs(calorieDeficit) / 7700.0).toString()

                // Display estimated weight change (loss or gain)
                if (calorieDeficit < 0) {
                    binding.monthlySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_gained)
                } else {
                    binding.monthlySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_lost)
                }

                // Initialize the monthly chart with food data and TDEE
                initMonthlyChart(foodList, userSettings.tdee)
            }
            viewModel.yearlyFoodListAndUserSettings.observe(viewLifecycleOwner) { foodListAndUserSettings ->
                val foodList = foodListAndUserSettings.first  // List of foods consumed during the year
                val userSettings = foodListAndUserSettings.second  // User settings (including TDEE)

                // Get the number of days in the year (365 or 366 for leap years)
                val today = LocalDate.now(ZoneId.systemDefault())  // Get the current date
                val daysInYear = if (today.isLeapYear) 366 else 365  // Adjust for leap year

                // Create an array to track total calories for each day of the year
                val dailyCalories = IntArray(daysInYear)  // Array for storing calories for each day of the year

                // Sum the calories for each day based on food entries
                for (food in foodList) {
                    val dayOfYear = getDayOfYear(food.date) - 1  // Adjust to 0-based index
                    if (dayOfYear in 0 until daysInYear) {  // Ensure it's within the correct bounds
                        dailyCalories[dayOfYear] += food.calories  // Add the food's calories to the appropriate day
                    }
                }

                // Define the days in each month (adjust February days for leap year)
                val daysInMonth = intArrayOf(
                    31,  // January
                    if (today.isLeapYear) 29 else 28,  // February
                    31,  // March
                    30,  // April
                    31,  // May
                    30,  // June
                    31,  // July
                    31,  // August
                    30,  // September
                    31,  // October
                    30,  // November
                    31   // December
                )

                // Compute cumulative days in the year to map months to days
                val cumulativeDaysInYear = IntArray(13)
                cumulativeDaysInYear[0] = 0
                for (i in 1..12) {
                    cumulativeDaysInYear[i] = cumulativeDaysInYear[i - 1] + daysInMonth[i - 1]
                }

                // Initialize variables
                val totalYearlyBMR = userSettings.tdee * daysInYear
                var totalCaloriesConsumed = totalYearlyBMR

                // Loop through each month to adjust total calories consumed
                for (monthIndex in 0 until 12) {
                    val daysInThisMonth = daysInMonth[monthIndex]
                    val monthlyBMR = userSettings.tdee * daysInThisMonth

                    val startDayOfYear = cumulativeDaysInYear[monthIndex]
                    val endDayOfYear = cumulativeDaysInYear[monthIndex + 1] - 1  // Inclusive range

                    // Check if the month has any entries
                    var hasEntriesForMonth = false
                    var totalCaloriesInMonth = 0

                    // Sum the calories logged in that month
                    for (food in foodList) {
                        val dayOfYear = getDayOfYear(food.date) - 1  // Adjust to 0-based index
                        if (dayOfYear in startDayOfYear..endDayOfYear) {
                            hasEntriesForMonth = true
                            totalCaloriesInMonth += food.calories
                        }
                    }

                    if (hasEntriesForMonth) {
                        // Adjust totalCaloriesConsumed: subtract monthly BMR and add logged calories
                        totalCaloriesConsumed -= monthlyBMR
                        totalCaloriesConsumed += totalCaloriesInMonth
                    }
                }

                // Calculate the total yearly deficit
                val totalYearlyDeficit = totalYearlyBMR - totalCaloriesConsumed

                // Estimate weight change (1 kg = 7700 calories)
                val weightChange = totalYearlyDeficit / 7700.0  // Weight change in kg

                // Update the yearly summary UI
                binding.yearlySummary.calories.text = totalCaloriesConsumed.toString()  // Total calories consumed in the year
                binding.yearlySummary.bmr.text = totalYearlyBMR.toString()  // Total yearly BMR (TDEE * days in year)
                binding.yearlySummary.calorieDeficit.text = totalYearlyDeficit.toString()
                binding.yearlySummary.weightChange.text = String.format("%.2f kg", weightChange)

                // Display estimated weight change (gain or loss)
                binding.yearlySummary.labelWeightChange.text = if (totalYearlyDeficit < 0) {
                    getString(R.string.estimated_weight_gained)
                } else {
                    getString(R.string.estimated_weight_lost)
                }

                // Initialize the yearly chart with food data and TDEE
                initYearlyChart(foodList, userSettings.tdee)
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initYearlyChart(foodList: List<Food>, tdee: Int) {
        // Find the LineChart by its ID
        val lineChart = binding.yearlyChart  // Assuming you have a yearly chart in your layout

        // Get the primary color from the theme using ResourcesUtil
        val primaryColor = ResourcesUtil.getThemeColor(requireContext(), android.R.attr.colorPrimary)

        // Create a list of entries for the LineChart (each entry represents a month's total calories)
        val lineEntries = ArrayList<Entry>()
        val bmrEntries = ArrayList<Entry>() // Entries for the TDEE line (BMR)

        // Create an array to track total calories for each month (12 months in a year)
        val monthlyCalories = IntArray(12)  // Array for storing total calories for each month

        // Get the current year and check if it's a leap year
        val isLeapYear = LocalDate.now().isLeapYear

        // Define the days in each month (adjust February days for leap year)
        val daysInMonth = intArrayOf(
            31,  // January
            if (isLeapYear) 29 else 28,  // February (29 days for leap year, otherwise 28)
            31,  // March
            30,  // April
            31,  // May
            30,  // June
            31,  // July
            31,  // August
            30,  // September
            31,  // October
            30,  // November
            31   // December
        )

        // Group food items by month and sum the calories for each month
        for (food in foodList) {
            val month = getMonthFromDate(food.date)  // Get the month (0 = January, 11 = December)
            if (month in 0..11) {
                monthlyCalories[month] += food.calories  // Add the food's calories to the appropriate month
            }
        }

        // Add entries for each month (January to December)
        for (i in 0 until 12) {
            // Get the total calories for the month (use TDEE if no calories were consumed that month)
            val caloriesForTheMonth = if (monthlyCalories[i] == 0) tdee * daysInMonth[i] else monthlyCalories[i]

            // Add the monthly calories entry
            lineEntries.add(Entry(i.toFloat(), caloriesForTheMonth.toFloat()))

            // Add BMR (TDEE) entry for each month (horizontal line at TDEE value for the whole month)
            bmrEntries.add(Entry(i.toFloat(), (tdee * daysInMonth[i]).toFloat()))
        }

        // Create LineDataSets for both the monthly calories and BMR
        val lineDataSet = LineDataSet(lineEntries, "Monthly Calories")
        val bmrDataSet = LineDataSet(bmrEntries, "BMR (TDEE)")

        // Customize the Monthly Calories LineDataSet
        lineDataSet.color = primaryColor
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.valueTextSize = 10f
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawFilled(false)

        // Customize the BMR (TDEE) LineDataSet
        bmrDataSet.color = primaryColor
        bmrDataSet.setDrawCircles(false)
        bmrDataSet.lineWidth = 2f
        bmrDataSet.valueTextColor = Color.BLACK

        // Set both datasets (Monthly Calories and BMR) to the LineChart
        val lineData = LineData(lineDataSet, bmrDataSet)
        lineChart.data = lineData

        // Set the Y-axis to start from 0
        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f

        // Set the X-axis labels for the months (January to December)
        val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // Optionally, disable the right Y-axis
        lineChart.axisRight.isEnabled = false

        // Refresh the chart to apply the changes
        lineChart.invalidate()
    }

    private fun getMonthFromDate(dateString: String): Int {
        // Example: "2024-11-15" -> 10 (November is month 10 in 0-based indexing)
        val date = LocalDate.parse(dateString)  // Parse the date string into a LocalDate
        return date.monthValue - 1  // Month value is 1-based (January = 1), so subtract 1 for 0-based indexing
    }

    private fun getDayOfYear(dateString: String): Int {
        // Example: "2024-11-15" -> 320 (November 15th is the 320th day of the year)
        val date = LocalDate.parse(dateString)  // Parse the date string into a LocalDate
        return date.dayOfYear - 1  // Return day of the year as 0-based index (0 to 365 or 0 to 364)
    }

    private fun initMonthlyChart(foodList: List<Food>, tdee: Int) {
        // Find the LineChart by its ID
        val lineChart = binding.monthlyChart  // Assuming you have a monthly chart in your layout

        // Get the primary color from the theme using ResourcesUtil
        val primaryColor = ResourcesUtil.getThemeColor(requireContext(), android.R.attr.colorPrimary)

        // Create a list of entries for the LineChart (each entry represents a day's total calories)
        val lineEntries = ArrayList<Entry>()
        val bmrEntries = ArrayList<Entry>() // Entries for the TDEE line (BMR)

        // Create an array to track total calories for each day of the month (31 days)
        val dailyCalories = IntArray(31)  // Array for storing total calories for each day

        // Get the current month
        val today = LocalDate.now(ZoneId.systemDefault())  // Get the current date
        val daysInMonth = today.lengthOfMonth()  // Get the number of days in the current month (28, 29, 30, or 31)

        Log.d("ChartDebug", "Days in Month: $daysInMonth")

        // Group food items by the day of the month and sum the calories for each day
        for (food in foodList) {
            val dayOfMonth = getDayOfMonth(food.date)  // Assuming the date is in a format that can be used here
            Log.d("ChartDebug", "Food Date: ${food.date}, Day of Month: $dayOfMonth")

            // Add the food's calories to the appropriate day
            if (dayOfMonth in 1..daysInMonth) {
                dailyCalories[dayOfMonth - 1] += food.calories
            }
        }

        // Log the daily calories for debugging
        Log.d("ChartDebug", "Daily Calories: ${dailyCalories.joinToString(", ")}")

        // Add entries for each day of the month (up to 31 days)
        for (i in 0 until daysInMonth) {
            val caloriesForTheDay = if (dailyCalories[i] == 0) tdee else dailyCalories[i]

            lineEntries.add(Entry(i.toFloat(), caloriesForTheDay.toFloat()))
            bmrEntries.add(Entry(i.toFloat(), tdee.toFloat()))
        }

        // Create LineDataSets for both the daily calories and BMR
        val lineDataSet = LineDataSet(lineEntries, "Daily Calories")
        val bmrDataSet = LineDataSet(bmrEntries, "BMR (TDEE)")

        lineDataSet.color = primaryColor
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.valueTextSize = 10f
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawFilled(false)

        bmrDataSet.color = primaryColor
        bmrDataSet.setDrawCircles(false)
        bmrDataSet.lineWidth = 2f
        bmrDataSet.valueTextColor = Color.BLACK

        // Set both datasets (Daily Calories and BMR) to the LineChart
        val lineData = LineData(lineDataSet, bmrDataSet)
        lineChart.data = lineData

        // Set the Y-axis to start from 0
        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f

        // Set the X-axis labels for the days of the month (1 to 31)
        val monthLabels = (1..daysInMonth).map { it.toString() }
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // Optionally, disable the right Y-axis
        lineChart.axisRight.isEnabled = false

        // Refresh the chart to apply the changes
        lineChart.invalidate()
    }

    private fun getDayOfMonth(dateString: String): Int {
        // Example: "2024-11-15" -> 15
        val date = LocalDate.parse(dateString)  // Parse the date string into a LocalDate
        return date.dayOfMonth  // Return the day of the month (1 to 31)
    }

    private fun initWeeklyChart(foodList: List<Food>, tdee: Int) {
        // Find the LineChart by its ID
        val lineChart = binding.weeklyChart

        // Get the primary color from the theme using ResourcesUtil
        val primaryColor = ResourcesUtil.getThemeColor(requireContext(), android.R.attr.colorPrimary)

        // Create a list of entries for the LineChart (each entry represents a day's total calories)
        val lineEntries = ArrayList<Entry>()
        val bmrEntries = ArrayList<Entry>() // Entries for the TDEE line (BMR)

        // Create an array to track total calories for each day (7 days in a week)
        val dailyCalories = IntArray(7) // Array for storing total calories for each day

        // Get today's date using ZonedDateTime in the local timezone, without time component
        val today = LocalDate.now(ZoneId.systemDefault())  // Get the current date in the local time zone
        val todayIndex = today.dayOfWeek.value % 7  // Get the 0-6 range for today (1 = Monday, 7 = Sunday)

        Log.d("ChartDebug", "Today Index: $todayIndex")  // Log today's index

        // Calculate the start of the week (i.e., Wednesday)
        val startOfWeekIndex = (todayIndex - 4 + 7) % 7  // Shift to Wednesday as the start of the week

        Log.d("ChartDebug", "Start of Week Index (Wednesday): $startOfWeekIndex")  // Log start of week index

        // Group food items by the day of the week and sum the calories for each day
        for (food in foodList) {
            // Parse the food's date to determine the day of the week (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
            val dayOfWeek = getDayOfWeek(food.date)  // Assuming the date is in a format that can be used here
            Log.d("ChartDebug", "Food Date: ${food.date}, Day of Week: $dayOfWeek")  // Log food date and day of week

            // Add the food's calories to the appropriate day
            val dayIndex = (dayOfWeek - 1 + 7) % 7  // Adjust for indexing (0 = Sunday, 6 = Saturday)
            if (dayIndex in 0..6) {
                dailyCalories[dayIndex] += food.calories
            }
        }

        // Log the daily calories for debugging
        Log.d("ChartDebug", "Daily Calories: ${dailyCalories.joinToString(", ")}")

        // Add 7 entries (one for each day) into the `lineEntries` list
        for (i in 0 until 7) {
            // Adjust the day index for chart placement (starting from Wednesday)
            val adjustedIndex = (startOfWeekIndex + i) % 7
            Log.d("ChartDebug", "Adding Entry for Day $i: ${dailyCalories[adjustedIndex]} calories at index $adjustedIndex")  // Log entry addition

            // Check if dailyCalories for the day is 0, and if so, use the TDEE value for that day
            val caloriesForTheDay = if (dailyCalories[adjustedIndex] == 0) tdee else dailyCalories[adjustedIndex]

            // Add the daily calories or TDEE as the entry for that day
            lineEntries.add(Entry(i.toFloat(), caloriesForTheDay.toFloat()))

            // Add BMR (TDEE) line entry for each day (horizontal line at tdee value)
            bmrEntries.add(Entry(i.toFloat(), tdee.toFloat()))
        }

        // Create LineDataSets for both the daily calories and BMR
        val lineDataSet = LineDataSet(lineEntries, "Daily Calories")
        val bmrDataSet = LineDataSet(bmrEntries, "BMR (TDEE)")

        // Customize the Daily Calories LineDataSet (you can adjust colors, line width, etc.)
        lineDataSet.color = primaryColor  // Set the color of the line
        lineDataSet.valueTextColor = Color.BLACK  // Set the color of the values (calories)
        lineDataSet.valueTextSize = 10f  // Set the text size of the values

        // Ensure the line is continuous even between entries with value 0
        lineDataSet.setDrawCircles(true) // Draw circles on each point (optional)
        lineDataSet.setDrawFilled(false) // Ensure that the line is drawn even if the values are zero

        // Customize the BMR (TDEE) LineDataSet
        bmrDataSet.color = primaryColor // Use the same primary color for the BMR line
        bmrDataSet.setDrawCircles(false) // Do not draw circles for the BMR line
        bmrDataSet.lineWidth = 2f // Set BMR line width
        bmrDataSet.valueTextColor = Color.BLACK // Set text color for the BMR line (optional)

        // Set both datasets (Daily Calories and BMR) to the LineChart
        val lineData = LineData(lineDataSet, bmrDataSet)

        // Set the data for the LineChart
        lineChart.data = lineData

        // Customize the chart (optional)
        lineChart.description.text = "Weekly Calories Intake"

        // Remove the grid lines (optional)
        lineChart.axisLeft.setDrawGridLines(true)  // You can keep or remove grid lines depending on your preference
        lineChart.axisRight.setDrawGridLines(false)

        // Set the Y-axis to start from 0
        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f // Ensure the Y-axis starts at 0

        // Generate dynamic day labels based on today
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weekLabels = mutableListOf<String>()
        for (i in 0 until 7) {
            val labelIndex = (startOfWeekIndex + i) % 7
            weekLabels.add(daysOfWeek[labelIndex])
        }

        Log.d("ChartDebug", "Week Labels: ${weekLabels.joinToString(", ")}")  // Log the week labels for debugging

        // Set the X-axis labels dynamically (based on today, with today in the center)
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(weekLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Ensure one label per day

        // Center the chart on today (today will be at the center)
        val offset = (todayIndex - 3 + 7) % 7  // Calculate the correct offset to center the chart on today
        Log.d("ChartDebug", "Offset: $offset")  // Log the offset calculation
        val totalEntries = 7
        val centerOffset = (totalEntries / 2) - offset
        Log.d("ChartDebug", "Center Offset: $centerOffset")  // Log the center offset for debugging
        lineChart.moveViewToX(centerOffset.toFloat())  // Adjust the X view to center on today

        // Optionally, disable the right Y-axis as it is typically not used in line charts
        lineChart.axisRight.isEnabled = false

        // Refresh the chart to apply the changes
        lineChart.invalidate()
    }

    // Helper function to get the day of the week from a date string (this example assumes the date is in "yyyy-MM-dd" format)
    private fun getDayOfWeek(dateString: String): Int {
        // Example: "2024-11-15" -> Friday
        val date = LocalDate.parse(dateString)
        return date.dayOfWeek.value  // 1 = Monday, 7 = Sunday
    }

    fun initDailyChart(totalCalories: Int, bmr: Int) {
        // Find the BarChart by its ID
        val barChart = binding.dailyChart

        // Get the primary color from the theme using ResourcesUtil
        val primaryColor =
            ResourcesUtil.getThemeColor(requireContext(), android.R.attr.colorPrimary)

        // Create a list of BarEntry objects (each BarEntry represents a bar in the chart)
        val barEntries = ArrayList<BarEntry>()
        barEntries.add(
            BarEntry(
                0f,
                totalCalories.toFloat()
            )
        )  // Total calories (x=0, y=totalCalories)
        barEntries.add(BarEntry(1f, bmr.toFloat()))  // BMR (x=1, y=bmr)

        // Create a BarDataSet from the list of BarEntries
        val barDataSet = BarDataSet(barEntries, "Calories vs BMR")

        // Check if TotalCalories is more than BMR and set the color of the Total Calories bar accordingly
        if (totalCalories > bmr) {
            // Set Total Calories bar color to red if it's above BMR
            barDataSet.colors = listOf(
                Color.RED,  // Total Calories (above BMR)
                primaryColor // BMR (primary color from theme)
            )
        } else {
            // Set Total Calories bar color to green if it's below BMR
            barDataSet.colors = listOf(
                Color.GREEN, // Total Calories (below BMR)
                primaryColor // BMR (primary color from theme)
            )
        }

        // Create BarData with the BarDataSet
        val barData = BarData(barDataSet)

        // Set the data for the BarChart
        barChart.data = barData

        // Customize the chart (optional)
        barChart.description.text = "Calories vs BMR"

        // Remove the top grid (top horizontal lines)
        barChart.axisLeft.setDrawGridLines(false)  // Disable grid lines on the left axis
        barChart.axisLeft.setDrawAxisLine(false)   // Optionally, you can also remove the axis line on the left axis
        barChart.axisRight.setDrawGridLines(false) // Disable grid lines on the right axis
        barChart.axisRight.setDrawAxisLine(false)  // Optionally, you can also remove the axis line on the right axis

        // Set the Y-axis to start from 0
        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f // Ensure the Y-axis starts at 0

        // Optionally, disable the right Y-axis as it is typically not used in bar charts
        barChart.axisRight.isEnabled = false

        // Refresh the chart to apply the changes
        barChart.invalidate()
    }
}
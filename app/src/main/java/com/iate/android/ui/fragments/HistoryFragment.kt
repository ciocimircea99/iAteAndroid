package com.iate.android.ui.fragments

import android.graphics.Color
import android.os.Bundle
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
                binding.dailySummary.weightChange.text = (totalCalories / 7700.0).toString()

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
                val foodList = foodListAndUserSettings.first
                val userSettings = foodListAndUserSettings.second

                val totalCalories = foodList.sumOf { it.calories }
                val calorieDeficit = (userSettings.tdee * 7) - totalCalories

                binding.weeklySummary.calories.text = totalCalories.toString()
                binding.weeklySummary.bmr.text = (userSettings.tdee * 7).toString()
                binding.weeklySummary.calorieDeficit.text = (calorieDeficit).toString()
                binding.weeklySummary.weightChange.text = (totalCalories / 7700.0).toString()

                if (calorieDeficit < 0) {
                    binding.weeklySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_gained)
                } else {
                    binding.weeklySummary.labelWeightChange.text =
                        getString(R.string.estimated_weight_lost)
                }

                initWeeklyChart(foodList, userSettings.tdee)
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initWeeklyChart(foodList: List<Food>, tdee: Int) {
        // Find the LineChart by its ID
        val lineChart = binding.weeklyChart

        // Get the primary color from the theme using ResourcesUtil
        val primaryColor =
            ResourcesUtil.getThemeColor(requireContext(), android.R.attr.colorPrimary)

        // Create a list of entries for the LineChart (each entry represents a day's total calories)
        val lineEntries = ArrayList<Entry>()
        val bmrEntries = ArrayList<Entry>() // Entries for the TDEE line (BMR)

        // Create an array to track total calories for each day (7 days in a week)
        val dailyCalories = IntArray(7) // Array for storing total calories for each day

        // Get today's day index (0 = Monday, ..., 6 = Sunday)
        val todayIndex =
            LocalDate.now().dayOfWeek.value % 7 // 1 = Monday, 7 = Sunday (converted to 0-6 range)

        // Calculate the start of the week (i.e., Wednesday)
        val startOfWeekIndex =
            (todayIndex - 3 + 7) % 7  // Shift to Wednesday as the start of the week

        // Group food items by the day of the week and sum the calories for each day
        for (food in foodList) {
            // Parse the food's date to determine the day of the week (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
            val dayOfWeek =
                getDayOfWeek(food.date)  // Assuming the date is in a format that can be used here

            // Add the food's calories to the appropriate day
            val dayIndex =
                (dayOfWeek - 1 + 7) % 7  // Adjust for indexing (0 = Sunday, 6 = Saturday)
            if (dayIndex in 0..6) {
                dailyCalories[dayIndex] += food.calories
            }
        }

        // Add 7 entries (one for each day) into the `lineEntries` list
        for (i in 0 until 7) {
            // Adjust the day index for chart placement (starting from Wednesday)
            val adjustedIndex = (startOfWeekIndex + i) % 7
            lineEntries.add(Entry(i.toFloat(), dailyCalories[adjustedIndex].toFloat()))

            // Add BMR (TDEE) line entry for each day (horizontal line)
            bmrEntries.add(Entry(i.toFloat(), tdee.toFloat()))
        }

        // Create LineDataSets for both the daily calories and BMR
        val lineDataSet = LineDataSet(lineEntries, "Daily Calories")
        val bmrDataSet = LineDataSet(bmrEntries, "BMR (TDEE)")

        // Customize the Daily Calories LineDataSet (you can adjust colors, line width, etc.)
        lineDataSet.color = primaryColor  // Set the color of the line
        lineDataSet.valueTextColor = Color.BLACK  // Set the color of the values (calories)
        lineDataSet.valueTextSize = 10f  // Set the text size of the values

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

        // Customize the X-axis (e.g., add day labels and center today)
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter =
            IndexAxisValueFormatter(listOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue"))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Ensure one label per day

        // Center the chart on today (today will be at the center)
        val offset = todayIndex - 3 // Center the chart so that today (Saturday) is in the center
        lineChart.moveViewToX(offset.toFloat())  // Move the view to center the chart on today

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
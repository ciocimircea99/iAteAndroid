package com.iate.android.ui.custom

import DateTimeUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.iate.android.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val selectedCalendar = Calendar.getInstance() // Tracks the selected date
    private val todayCalendar = Calendar.getInstance() // Tracks today's date
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEE\nd", Locale.getDefault())

    private lateinit var selectedDateView: TextView
    private lateinit var daysContainer: LinearLayout
    private val dayViews = mutableListOf<TextView>()

    // Listener for date selection changes
    var dateSelectedListener: OnDateSelectedListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_date_selector, this, true)

        selectedDateView = findViewById(R.id.selectedDate)
        daysContainer = findViewById(R.id.daysContainer)

        findViewById<View>(R.id.arrowLeft).setOnClickListener { changeSelectedDate(-1) }
        findViewById<View>(R.id.arrowRight).setOnClickListener { changeSelectedDate(1) }

        initializeDays()
        updateDisplayedDates()
    }

    private fun initializeDays() {
        // Set the initial range: 7 days, with the current day in the middle
        daysContainer.removeAllViews()
        dayViews.clear()

        // Populate the 7 fixed day views
        for (i in -3..3) {
            val dayView = TextView(context).apply {
                textAlignment = TEXT_ALIGNMENT_CENTER
                setPadding(16, 16, 16, 16)
                setOnClickListener { onDayClicked(i) }
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            dayViews.add(dayView)
            daysContainer.addView(dayView)
        }
    }

    private fun updateDisplayedDates() {
        // Update each day in the fixed 7-day range, centered around today's date
        val displayCalendar = Calendar.getInstance().apply {
            time = todayCalendar.time // Start from today's date
            add(Calendar.DAY_OF_YEAR, -3) // Set to the start of the range
        }

        for (i in dayViews.indices) {
            val dayView = dayViews[i]
            dayView.text = dayFormat.format(displayCalendar.time)

            // Reset styles first
            dayView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            dayView.background = null

            // Highlight the selected day with a gradient background
            if (isSameDay(displayCalendar, selectedCalendar)) {
                dayView.setBackgroundResource(R.drawable.background_primary_gradient)
                dayView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            // Mark today differently if it's within the 7-day range
            else if (isSameDay(displayCalendar, todayCalendar)) {
                dayView.setBackgroundResource(R.drawable.background_primary_gradient)
                dayView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }

            // Prepare displayCalendar for the next day in the range
            displayCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Update the selected date display below
        selectedDateView.text = dateFormat.format(selectedCalendar.time)

        // Notify the listener of the new selected date in ISO format
        dateSelectedListener?.onDateSelected(DateTimeUtil.millisToIso8601(selectedCalendar.time.time))
    }

    private fun onDayClicked(offset: Int) {
        // Calculate the clicked day based on today's date + offset and update selected date
        val newSelectedCalendar = Calendar.getInstance().apply {
            time = todayCalendar.time
            add(Calendar.DAY_OF_YEAR, offset)
        }
        selectedCalendar.time = newSelectedCalendar.time
        updateDisplayedDates() // Refresh to highlight the new selected date
    }

    private fun changeSelectedDate(offset: Int) {
        // Move the selected date by the offset (left or right arrow click) without shifting the 7-day range
        selectedCalendar.add(Calendar.DAY_OF_YEAR, offset)

        // Ensure selected date only updates the highlight within the fixed 7-day range
        updateDisplayedDates()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Interface to notify date changes in ISO 8601 format
    interface OnDateSelectedListener {
        fun onDateSelected(date: String) // Pass date as ISO 8601 formatted string
    }
}

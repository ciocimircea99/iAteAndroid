package com.iate.android.util

import android.content.Context
import android.util.TypedValue
import androidx.core.content.ContextCompat

object ResourcesUtil {

    // Method to get the theme color for a given attribute
    fun getThemeColor(context: Context, colorAttr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }
}
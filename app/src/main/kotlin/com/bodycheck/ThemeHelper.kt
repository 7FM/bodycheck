package com.bodycheck

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

object ThemeHelper {

    fun applyGenderTheme(activity: AppCompatActivity) {
        val prefs = activity.getSharedPreferences("bodycheck_prefs", AppCompatActivity.MODE_PRIVATE)
        val isFemale = prefs.getString("gender", "male") == "female"

        val primaryColor = ContextCompat.getColor(
            activity, if (isFemale) R.color.primary_female else R.color.primary
        )
        val primaryDarkColor = ContextCompat.getColor(
            activity, if (isFemale) R.color.primary_dark_female else R.color.primary_dark
        )

        val isDarkMode = (activity.resources.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = !isDarkMode

        activity.findViewById<Toolbar>(R.id.toolbar)?.setBackgroundColor(primaryColor)
    }

    fun applyThemeMode(context: android.content.Context) {
        val prefs = context.getSharedPreferences("bodycheck_prefs", android.content.Context.MODE_PRIVATE)
        val mode = prefs.getString("theme_mode", "system")
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}

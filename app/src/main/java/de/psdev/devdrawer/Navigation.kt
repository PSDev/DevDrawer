package de.psdev.devdrawer

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey

@get:StringRes
val NavKey.title
    get(): Int = when (this) {
        is SettingsRoute -> R.string.settings
        is WidgetListRoute -> R.string.widgets
        is WidgetProfilesRoute -> R.string.profiles
        is WidgetEditorRoute -> R.string.edit_widget
        is WidgetProfileEditorRoute -> R.string.edit_profile
        is AboutRoute -> R.string.app_info
        else -> R.string.app_name
    }

package de.psdev.devdrawer

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WidgetListRoute : NavKey

@Serializable
data object WidgetProfilesRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object AboutRoute : NavKey

@Serializable
data class WidgetEditorRoute(val id: Int) : NavKey

@Serializable
data class WidgetProfileEditorRoute(val id: String) : NavKey

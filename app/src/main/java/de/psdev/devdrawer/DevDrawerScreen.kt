package de.psdev.devdrawer

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile

sealed class DevDrawerScreen(
    val route: String
)

sealed class TopLevelScreen(route: String): DevDrawerScreen(route) {
    abstract val icon: ImageVector
    @get:StringRes abstract val label: Int
}

object Widgets: TopLevelScreen(
    route = "widgets"
) {
    override val icon: ImageVector = Icons.Filled.Widgets
    override val label: Int = R.string.widgets
}

object Profiles: TopLevelScreen(
    route = "profiles"
) {
    override val icon: ImageVector = Icons.Filled.Grid3x3
    override val label: Int = R.string.profiles
}

object Settings: TopLevelScreen(
    route = "settings"
) {
    override val icon: ImageVector = Icons.Filled.Settings
    override val label: Int = R.string.settings
}

object AppInfo: TopLevelScreen(
    route = "info"
) {
    override val icon: ImageVector = Icons.Filled.Info
    override val label: Int = R.string.app_info
}

data class WidgetEditorDestination(
    val widget: Widget
): DevDrawerScreen(route = "widgets/${widget.id}")

data class ProfileEditorDestination(
    val widgetProfile: WidgetProfile
): DevDrawerScreen(route = "profiles/${widgetProfile.id}")

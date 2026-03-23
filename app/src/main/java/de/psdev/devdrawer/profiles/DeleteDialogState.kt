package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile

sealed class DeleteDialogState {
    data object Hidden : DeleteDialogState()
    data class Showing(
        val widgetProfile: WidgetProfile
    ) : DeleteDialogState()

    data class InUseError(
        val widgetProfile: WidgetProfile,
        val widgets: List<Widget>
    ) : DeleteDialogState()
}
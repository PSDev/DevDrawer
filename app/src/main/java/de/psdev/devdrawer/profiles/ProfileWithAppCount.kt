package de.psdev.devdrawer.profiles

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.database.WidgetProfile

/** A profile with the number of installed apps it currently matches. */
@Immutable
data class ProfileWithAppCount(val profile: WidgetProfile, val appCount: Int)

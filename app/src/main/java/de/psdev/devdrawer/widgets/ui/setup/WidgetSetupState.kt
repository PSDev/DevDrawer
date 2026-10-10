package de.psdev.devdrawer.widgets.ui.setup

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.profiles.ProfileWithAppCount

enum class SetupSource { MY_APPS, PATTERN, PROFILE }

@Immutable
data class SetupApp(
    val name: String,
    val packageName: String,
    val signatureHash: String,
    /** Other installed apps signed with the same key, which a signature filter on this app also shows. */
    val otherAppsWithSameKey: Int,
    val icon: Drawable?
)

@Immutable
data class WidgetSetupState(
    val isLoading: Boolean = true,
    val source: SetupSource = SetupSource.MY_APPS,
    val apps: List<SetupApp> = emptyList(),
    val selectedPackageName: String? = null,
    val pattern: String = "",
    val profiles: List<ProfileWithAppCount> = emptyList(),
    val selectedProfileId: String? = null,
    /** How many installed apps the widget will list with the current choice. */
    val matchCount: Int = 0,
    /** Done was tapped and the profile and widget are being saved. */
    val isSaving: Boolean = false
) {
    val canFinish: Boolean
        get() = !isSaving && when (source) {
            SetupSource.MY_APPS -> selectedPackageName != null
            SetupSource.PATTERN -> pattern.isNotBlank()
            SetupSource.PROFILE -> selectedProfileId != null
        }
}

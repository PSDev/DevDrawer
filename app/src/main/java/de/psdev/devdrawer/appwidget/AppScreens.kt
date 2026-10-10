package de.psdev.devdrawer.appwidget

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.AppIcon

/** One screen of an app that can be opened directly. */
data class AppScreen(
    val className: String,
    val label: String,
    /** The class name relative to the app's package, like ".settings.SettingsActivity". */
    val shortName: String,
    /** The screen the launcher opens. */
    val isMain: Boolean
)

/**
 * The screens of [packageName] that another app may open: exported, enabled and not behind a permission.
 * The launcher's screen comes first, then the rest by label.
 */
fun appScreens(
    packageName: String,
    activities: List<ActivityInfo>,
    mainClassName: String?,
    labelOf: (ActivityInfo) -> String
): List<AppScreen> = activities
    .filter { it.exported && it.enabled && it.permission == null }
    .map { activity ->
        AppScreen(
            className = activity.name,
            label = labelOf(activity),
            shortName = if (activity.name.startsWith("$packageName.")) activity.name.removePrefix(packageName) else activity.name,
            isMain = activity.name == mainClassName
        )
    }
    .sortedWith(compareByDescending<AppScreen> { it.isMain }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.label })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseScreenSheet(
    appName: String,
    appIcon: Drawable?,
    screens: List<AppScreen>,
    onScreenSelected: (AppScreen) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIcon(icon = appIcon, size = 40.dp)
            Text(text = stringResource(R.string.open_app_screen, appName), style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
            items(screens, key = { it.className }) { screen ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScreenSelected(screen) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (screen.isMain) FontWeight.Medium else null
                    )
                    Text(
                        text = if (screen.isMain) stringResource(R.string.main_screen, screen.shortName) else screen.shortName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

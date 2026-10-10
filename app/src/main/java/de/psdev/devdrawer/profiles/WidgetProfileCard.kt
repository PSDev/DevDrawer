package de.psdev.devdrawer.profiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.utils.DefaultPreviews

/**
 * A profile in the Profiles list: its name, how many filters and apps it has, and the widgets using it.
 * Tapping opens it; the overflow menu duplicates or deletes it.
 */
@Composable
fun WidgetProfileCard(
    summary: ProfileSummary,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onDuplicateClick: (WidgetProfile) -> Unit = {},
    onDeleteClick: (WidgetProfile) -> Unit = {}
) {
    val profile = summary.profile
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { onWidgetProfileClick(profile) }
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = if (summary.filterCount == 0) {
                        stringResource(R.string.profile_no_filters)
                    } else {
                        stringResource(
                            R.string.profile_counts,
                            pluralStringResource(R.plurals.profile_filter_count, summary.filterCount, summary.filterCount),
                            pluralStringResource(R.plurals.widget_app_count, summary.appCount, summary.appCount)
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (summary.appCount == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (summary.usedBy.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(4.dp))
                    }
                    Text(
                        text = if (summary.usedBy.isEmpty()) {
                            stringResource(R.string.profile_not_used)
                        } else {
                            stringResource(R.string.profile_used_by, summary.usedBy.joinToString())
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                var menuOpen by remember { mutableStateOf(false) }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.profile_more_options, profile.name)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.duplicate)) },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDuplicateClick(profile)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDeleteClick(profile)
                        }
                    )
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun Preview_WidgetProfileCard() {
    DevDrawerTheme {
        Column {
            WidgetProfileCard(ProfileSummary(WidgetProfile(name = "Signed like DevDrawer2"), 1, 7, listOf("Work apps")))
            WidgetProfileCard(ProfileSummary(WidgetProfile(name = "Default"), 0, 0, emptyList()))
        }
    }
}

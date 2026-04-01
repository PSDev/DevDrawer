package de.psdev.devdrawer.profiles

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.utils.DefaultPreviews
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetProfileCard(
    widgetProfile: WidgetProfile,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onWidgetProfileLongClick: (WidgetProfile) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .combinedClickable(
                onClick = { onWidgetProfileClick(widgetProfile) },
                onLongClick = { onWidgetProfileLongClick(widgetProfile) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                text = widgetProfile.name
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = stringResource(id = R.string.last_modified)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    style = MaterialTheme.typography.bodySmall,
                    text = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(widgetProfile.updatedAt.atZone(ZoneId.systemDefault()))
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun Preview_WidgetProfileCard() {
    DevDrawerTheme {
        WidgetProfileCard(widgetProfile = WidgetProfile(name = "Test profile"))
    }
}
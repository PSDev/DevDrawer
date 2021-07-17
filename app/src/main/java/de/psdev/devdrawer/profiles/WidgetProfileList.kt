package de.psdev.devdrawer.profiles

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun WidgetProfileList(
    widgetProfiles: List<WidgetProfile>,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onWidgetProfileLongClick: (WidgetProfile) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        items(widgetProfiles, key = { it.id }) { widgetProfile ->
            WidgetProfileCard(
                widgetProfile = widgetProfile,
                onWidgetProfileClick = onWidgetProfileClick,
                onWidgetProfileLongClick = onWidgetProfileLongClick
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileList() {
    DevDrawerTheme {
        WidgetProfileList(
            listOf(
                WidgetProfile(name = "Profile 1"),
                WidgetProfile(name = "Profile 2")
            )
        )
    }
}
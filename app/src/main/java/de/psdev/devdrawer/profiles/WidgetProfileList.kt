package de.psdev.devdrawer.profiles

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun WidgetProfileList(
    profiles: List<ProfileSummary>,
    modifier: Modifier = Modifier,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onDuplicateProfile: (WidgetProfile) -> Unit = {},
    onDeleteProfile: (WidgetProfile) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
    ) {
        items(profiles, key = { it.profile.id }) { summary ->
            WidgetProfileCard(
                summary = summary,
                onWidgetProfileClick = onWidgetProfileClick,
                onDuplicateClick = onDuplicateProfile,
                onDeleteClick = onDeleteProfile
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
                ProfileSummary(WidgetProfile(name = "Profile 1"), 1, 7, listOf("Work apps")),
                ProfileSummary(WidgetProfile(name = "Profile 2"), 0, 0, emptyList())
            )
        )
    }
}

package de.psdev.devdrawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import de.psdev.devdrawer.about.AboutScreen
import de.psdev.devdrawer.profiles.ui.editor.WidgetProfileEditor
import de.psdev.devdrawer.profiles.ui.list.WidgetProfilesScreen
import de.psdev.devdrawer.settings.SettingsScreen
import de.psdev.devdrawer.widgets.ui.editor.WidgetEditorScreen
import de.psdev.devdrawer.widgets.ui.list.WidgetListScreen

@Composable
fun DevDrawerHost(
    navigationState: NavigationState,
    navigator: Navigator,
    menuCallback: AppBarActionsProvider,
    modifier: Modifier = Modifier
) {
    val entryProvider = entryProvider {
        entry<WidgetListRoute> {
            WidgetListScreen(
                onWidgetClick = { widget ->
                    navigator.navigate(WidgetEditorRoute(widget.id))
                }
            )
        }
        entry<WidgetProfilesRoute> {
            WidgetProfilesScreen(
                onEditProfile = { profile ->
                    navigator.navigate(WidgetProfileEditorRoute(profile.id))
                }
            )
        }
        entry<SettingsRoute> {
            SettingsScreen(
                onAboutClick = {
                    navigator.navigate(AboutRoute)
                }
            )
        }
        entry<AboutRoute> {
            AboutScreen()
        }
        entry<WidgetEditorRoute> { key ->
            WidgetEditorScreen(
                id = key.id,
                menuCallback = menuCallback,
                onBack = { navigator.goBack() },
                onEditWidgetProfile = { profile ->
                    navigator.navigate(WidgetProfileEditorRoute(profile.id))
                }
            )
        }
        entry<WidgetProfileEditorRoute> { key ->
            WidgetProfileEditor(
                profileId = key.id,
                menuCallback = menuCallback,
                onBack = { navigator.goBack() }
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        modifier = modifier
    )
}

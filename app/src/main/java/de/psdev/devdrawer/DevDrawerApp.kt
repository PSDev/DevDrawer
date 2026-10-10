package de.psdev.devdrawer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.analytics.AnalyticsOptInCard
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.analytics.showsAnalyticsOptIn
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.settings.SettingsViewModel
import de.psdev.devdrawer.settings.ThemeSetting
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevDrawerApp(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigationState: NavigationState,
    navigator: Navigator,
    trackingService: TrackingService,
    onWidgetSetupDone: (Widget) -> Unit = { navigator.goBack() },
    onWidgetSetupBack: (Int) -> Unit = { navigator.goBack() }
) {
    val settings by viewModel.persistedSettings.collectAsState()
    val darkTheme = when (settings.themeSetting) {
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val optInThanksMessage = stringResource(id = R.string.analytics_opt_in_thanks)
    val okLabel = stringResource(id = R.string.ok)
    val scope = rememberCoroutineScope()

    DevDrawerTheme(
        darkTheme = darkTheme,
        dynamicColor = settings.dynamicColorEnabled
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val currentRoute =
                navigationState.backStacks[navigationState.topLevelRoute]?.last() ?: navigationState.topLevelRoute

            val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            val navigationIcon: @Composable () -> Unit =
                if (currentRoute !in topLevelRoutes) {
                    {
                        IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                } else {
                    {}
                }

            BackHandler(enabled = currentRoute !in topLevelRoutes) {
                navigator.goBack()
            }

            val needsOptIn by trackingService.needsOptIn.collectAsState()
            val onOptIn = {
                trackingService.optIn()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = optInThanksMessage,
                        actionLabel = okLabel,
                        duration = SnackbarDuration.Long
                    )
                }
                Unit
            }

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        navigationIcon = navigationIcon,
                        title = {
                            Text(text = stringResource(id = currentRoute.title))
                        }
                    )
                },
                content = { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        if (showsAnalyticsOptIn(needsOptIn, currentRoute)) {
                            AnalyticsOptInCard(onOptIn = onOptIn, onOptOut = trackingService::optOut)
                        }
                        DevDrawerHost(
                            navigationState = navigationState,
                            navigator = navigator,
                            modifier = Modifier.weight(1f),
                            onWidgetSetupDone = onWidgetSetupDone,
                            onWidgetSetupBack = onWidgetSetupBack
                        )
                    }
                },
                bottomBar = {
                    NavigationBar {
                        BottomBarDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = navigationState.topLevelRoute == destination.route,
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(destination.label)) },
                                onClick = {
                                    navigator.navigate(destination.route)
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}

val topLevelRoutes = listOf(
    WidgetListRoute,
    WidgetProfilesRoute,
    SettingsRoute
)

enum class BottomBarDestination(
    val route: androidx.navigation3.runtime.NavKey,
    val icon: ImageVector,
    @param:StringRes val label: Int
) {
    Widgets(WidgetListRoute, Icons.Default.Widgets, R.string.widgets),
    Profiles(WidgetProfilesRoute, Icons.Default.Grid3x3, R.string.profiles),
    Settings(SettingsRoute, Icons.Default.Settings, R.string.settings)
}

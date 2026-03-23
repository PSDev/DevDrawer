package de.psdev.devdrawer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.settings.SettingsViewModel
import de.psdev.devdrawer.settings.ThemeSetting
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevDrawerApp(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigationState: NavigationState,
    navigator: Navigator,
    trackingService: TrackingService
) {
    val settings by viewModel.persistedSettings.collectAsState()
    val darkTheme = when (settings.themeSetting) {
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
    }

    val snackbarHostState = remember { SnackbarHostState() }
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

            val (menuComposable, setMenu) = remember {
                mutableStateOf<AppBarActions?>(null)
            }

            // Reset state on navigation change
            LaunchedEffect(currentRoute) {
                setMenu(null)
            }

            val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            val navigationIcon: @Composable () -> Unit =
                if (currentRoute !in topLevelRoutes) {
                    {
                        IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                } else {
                    {}
                }

            BackHandler(enabled = currentRoute !in topLevelRoutes) {
                navigator.goBack()
            }

            val needsOptIn by trackingService.needsOptIn.collectAsState()
            if (needsOptIn) {
                AnalyticsOptInDialog(
                    onOptIn = {
                        trackingService.optIn()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Thank you! You can change your decision anytime on the settings tab.",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
                            )
                        }
                    },
                    onOptOut = {
                        trackingService.optOut()
                    }
                )
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
                        },
                        actions = {
                            menuComposable?.invoke(this)
                        }
                    )
                },
                content = { innerPadding ->
                    DevDrawerHost(
                        navigationState = navigationState,
                        navigator = navigator,
                        menuCallback = setMenu,
                        modifier = Modifier.padding(innerPadding)
                    )
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

@Composable
fun AnalyticsOptInDialog(
    onOptIn: () -> Unit,
    onOptOut: () -> Unit
) {
    // Persist the dialog-show timestamp across config changes so rotation doesn't restart the delay.
    val shownAt by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var buttonsEnabled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!buttonsEnabled) {
            val remaining = 2500L - (System.currentTimeMillis() - shownAt)
            if (remaining > 0) delay(remaining)
            buttonsEnabled = true
        }
    }

    AlertDialog(
        onDismissRequest = { /* Not cancelable */ },
        title = {
            Text(text = "Usage analytics", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = "In order for us to be able to better understand your use of the app we would like to analyse your usage.\n\n" +
                        "We use Firebase Analytics to track opened screens and certain interactions.\n\n" +
                        "We don't store personally identifiable data.\n\n" +
                        "Additionally we use Firebase Crashlytics for app crashes.\n\n" +
                        "Thank you for considering!"
            )
        },
        confirmButton = {
            TextButton(
                enabled = buttonsEnabled,
                onClick = onOptIn
            ) {
                Text("Opt-in")
            }
        },
        dismissButton = {
            TextButton(
                enabled = buttonsEnabled,
                onClick = onOptOut
            ) {
                Text("Opt-out")
            }
        }
    )
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

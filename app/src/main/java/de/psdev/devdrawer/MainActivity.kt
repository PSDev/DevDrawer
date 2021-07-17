package de.psdev.devdrawer

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.DevDrawerScreen.*
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.profiles.ui.editor.WidgetProfileEditor
import de.psdev.devdrawer.profiles.ui.list.WidgetProfilesScreen
import de.psdev.devdrawer.settings.SettingsScreen
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.widgets.ui.editor.WidgetEditor
import de.psdev.devdrawer.widgets.ui.list.WidgetListScreen
import kotlinx.coroutines.delay
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {

    companion object: KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DevDrawerApp()
        }
        val start = System.currentTimeMillis()
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object: ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    return if (System.currentTimeMillis() - start > 500L) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )
        lifecycleScope.launchWhenCreated {
            delay(600L)
            content.postInvalidate()
        }

        lifecycleScope.launchWhenResumed {
            trackingService.checkOptIn(this@MainActivity)
        }
    }
}

@Composable
fun DevDrawerApp() {
    DevDrawerTheme {
        Surface(color = MaterialTheme.colors.background) {
            ProvideWindowInsets {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(modifier = Modifier.statusBarsPadding()) {
                            Text(text = stringResource(id = R.string.app_name))
                        }
                    },
                    content = {
                        DevDrawerHost(navController, modifier = Modifier.padding(it))
                    },
                    bottomBar = {
                        BottomNavigation(modifier = Modifier.navigationBarsWithImePadding()) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val topLevel = listOf(
                                Widgets,
                                Profiles,
                                Settings,
                                AppInfo
                            )
                            topLevel.forEach { screen ->
                                BottomNavigationItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(stringResource(screen.label)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DevDrawerHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Widgets.route,
        modifier = modifier
    ) {
        composable(Widgets.route) {
            WidgetListScreen(
                navController = navController,
                onWidgetClick = { widget ->
                    navController.navigate(WidgetEditorDestination(widget).route)
                }
            )
        }
        composable(Widgets.route + "/{widgetId}", arguments = listOf(
            navArgument("widgetId") {
                type = NavType.IntType
            }
        )) {
            WidgetEditor()
        }
        composable(Profiles.route) {
            WidgetProfilesScreen(
                editProfile = { navController.navigate(ProfileEditorDestination(it).route) }
            )
        }
        composable("profiles/{profileId}", arguments = listOf(
            navArgument("profileId") {
                type = NavType.StringType
            }
        )) {
            WidgetProfileEditor()
        }
        composable(Settings.route) {
            SettingsScreen()
        }
        composable(AppInfo.route) {

        }
    }

}

@Preview
@Composable
fun Preview_DevDrawerApp() {
    DevDrawerApp()
}


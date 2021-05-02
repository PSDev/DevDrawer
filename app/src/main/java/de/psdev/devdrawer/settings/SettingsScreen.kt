package de.psdev.devdrawer.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun SettingsScreen() {
    SettingsScreen(
        viewModel = hiltViewModel()
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val viewState by viewModel.viewState.collectAsState()

    SettingsScreen(
        viewState = viewState,
        onActivityChooserChanged = {
            viewModel.onActivityChooserChanged(it)
        },
        onSortOrderChanged = {
            viewModel.onSortOrderChanged(it)
        },
        onAnalyticsOptInChanged = {
            viewModel.onAnalyticsOptInChanged(it)
        }

    )
}

@Composable
fun SettingsScreen(
    viewState: SettingsViewModel.ViewState,
    onActivityChooserChanged: (Boolean) -> Unit = {},
    onSortOrderChanged: (SortOrder) -> Unit = {},
    onAnalyticsOptInChanged: (Boolean) -> Unit = {},
) {
    when (val state = viewState) {
        SettingsViewModel.ViewState.Loading -> LoadingView()
        is SettingsViewModel.ViewState.Loaded -> {
            val settings = state.settings
            Column {
                SwitchPreference(text = stringResource(id = R.string.pref_show_activity_choice_title), enabled = settings.activityChooserEnabled) {
                    onActivityChooserChanged(it)
                }
                Divider()
                val labels = stringArrayResource(id = R.array.sort_order_labels)
                ListPreference(
                    label = stringResource(id = R.string.pref_sort_order_title),
                    values = SortOrder.values().mapIndexed { index, sortOrder ->
                        sortOrder to labels[index]
                    }.toMap(),
                    currentValue = settings.defaultSortOrder
                ) {
                    onSortOrderChanged(it)
                }
                Divider()
                AnimatedVisibility(visible = state.analyticsVisible) {
                    SwitchPreference(text = stringResource(id = R.string.pref_feature_analytics_opted_in_title), enabled = settings.analyticsOptIn) {
                        onAnalyticsOptInChanged(it)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview_SettingsScreen() {
    DevDrawerTheme {
        SettingsScreen(
            viewState = SettingsViewModel.ViewState.Loaded(
                settings = SettingsViewModel.Settings(
                    activityChooserEnabled = true,
                    defaultSortOrder = SortOrder.LAST_UPDATED,
                    analyticsOptIn = true
                ),
                analyticsVisible = true
            )
        )
    }
}
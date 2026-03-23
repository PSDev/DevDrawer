package de.psdev.devdrawer.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    SettingsScreen(
        viewState = viewState,
        onAboutClick = onAboutClick,
        onActivityChooserChanged = {
            viewModel.onActivityChooserChanged(it)
        },
        onSortOrderChanged = {
            viewModel.onSortOrderChanged(it)
        },
        onThemeSettingChanged = {
            viewModel.onThemeSettingChanged(it)
        },
        onDynamicColorChanged = {
            viewModel.onDynamicColorChanged(it)
        },
        onAnalyticsOptInChanged = {
            viewModel.onAnalyticsOptInChanged(it)
        }
    )
}

@Composable
fun SettingsScreen(
    viewState: SettingsViewModel.ViewState,
    onAboutClick: () -> Unit = {},
    onActivityChooserChanged: (Boolean) -> Unit = {},
    onSortOrderChanged: (SortOrder) -> Unit = {},
    onThemeSettingChanged: (ThemeSetting) -> Unit = {},
    onDynamicColorChanged: (Boolean) -> Unit = {},
    onAnalyticsOptInChanged: (Boolean) -> Unit = {},
) {
    when (viewState) {
        SettingsViewModel.ViewState.Loading -> LoadingView(modifier = Modifier.fillMaxSize())
        is SettingsViewModel.ViewState.Loaded -> {
            val settings = viewState.settings
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceCategory(title = stringResource(id = R.string.settings_category_general))
                SwitchPreference(
                    text = stringResource(id = R.string.pref_show_activity_choice_title),
                    enabled = settings.activityChooserEnabled
                ) {
                    onActivityChooserChanged(it)
                }
                HorizontalDivider()
                val sortOrderLabels = stringArrayResource(id = R.array.sort_order_labels)
                ListPreference(
                    label = stringResource(id = R.string.pref_sort_order_title),
                    values = SortOrder.entries.mapIndexed { index, sortOrder ->
                        sortOrder to sortOrderLabels[index]
                    }.toMap(),
                    currentValue = settings.defaultSortOrder
                ) {
                    onSortOrderChanged(it)
                }

                PreferenceCategory(title = stringResource(id = R.string.settings_category_ui))
                val themeLabels = stringArrayResource(id = R.array.theme_labels)
                ListPreference(
                    label = stringResource(id = R.string.pref_theme_title),
                    values = ThemeSetting.entries.mapIndexed { index, themeSetting ->
                        themeSetting to themeLabels[index]
                    }.toMap(),
                    currentValue = settings.themeSetting
                ) {
                    onThemeSettingChanged(it)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider()
                    SwitchPreference(
                        text = stringResource(id = R.string.pref_dynamic_color_title),
                        summary = stringResource(id = R.string.pref_dynamic_color_summary),
                        enabled = settings.dynamicColorEnabled
                    ) {
                        onDynamicColorChanged(it)
                    }
                }

                AnimatedVisibility(visible = viewState.analyticsVisible) {
                    Column {
                        PreferenceCategory(title = stringResource(id = R.string.settings_category_analytics))
                        SwitchPreference(
                            text = stringResource(id = R.string.pref_feature_analytics_opted_in_title),
                            enabled = settings.analyticsOptIn
                        ) {
                            onAnalyticsOptInChanged(it)
                        }
                    }
                }

                PreferenceCategory(title = stringResource(id = R.string.settings_category_about))
                Text(
                    text = stringResource(id = R.string.app_info),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAboutClick() }
                        .padding(16.dp)
                )
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
                    themeSetting = ThemeSetting.SYSTEM,
                    dynamicColorEnabled = true,
                    analyticsOptIn = true
                ),
                analyticsVisible = true
            )
        )
    }
}

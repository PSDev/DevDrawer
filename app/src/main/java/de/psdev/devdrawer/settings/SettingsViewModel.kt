package de.psdev.devdrawer.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.config.RemoteConfigService
import de.psdev.devdrawer.receivers.UpdateReceiver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mu.KLogging
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val remoteConfigService: RemoteConfigService,
    private val sharedPreferences: SharedPreferences
): ViewModel() {
    companion object: KLogging()

    val persistedSettings: StateFlow<Settings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            trySendBlocking(sharedPreferences.loadSettings(application))
        }
        send(sharedPreferences.loadSettings(application))
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sharedPreferences.loadSettings(application))

    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)

    init {
        viewModelScope.launch {
            val analyticsEnabled = remoteConfigService.getBoolean(TrackingService.CONFIG_KEY_ENABLED)
            persistedSettings.collect { settings ->
                viewState.value = ViewState.Loaded(
                    analyticsVisible = analyticsEnabled,
                    settings = settings
                )
            }
        }
    }

    fun onActivityChooserChanged(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PreferenceKeys.SHOW_ACTIVITY_CHOICE, enabled) }
        onSettingsUpdated()
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        sharedPreferences.edit { putString(PreferenceKeys.SORT_ORDER, sortOrder.name) }
        onSettingsUpdated()
    }

    fun onThemeSettingChanged(themeSetting: ThemeSetting) {
        sharedPreferences.edit { putString(PreferenceKeys.THEME, themeSetting.name) }
        onSettingsUpdated()
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PreferenceKeys.DYNAMIC_COLOR, enabled) }
        onSettingsUpdated()
    }

    fun onAnalyticsOptInChanged(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PreferenceKeys.ANALYTICS_OPT_IN, enabled) }
        onSettingsUpdated()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun onSettingsUpdated() {
        UpdateReceiver.send(application)
    }

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val analyticsVisible: Boolean,
            val settings: Settings
        ): ViewState()
    }

    data class Settings(
        val activityChooserEnabled: Boolean,
        val defaultSortOrder: SortOrder,
        val themeSetting: ThemeSetting,
        val dynamicColorEnabled: Boolean,
        val analyticsOptIn: Boolean
    )

    private fun SharedPreferences.loadSettings(application: Application): Settings = Settings(
        activityChooserEnabled = getBoolean(
            PreferenceKeys.SHOW_ACTIVITY_CHOICE,
            application.resources.getBoolean(de.psdev.devdrawer.R.bool.pref_show_activity_choice_default)
        ),
        defaultSortOrder = SortOrder.valueOf(
            getString(PreferenceKeys.SORT_ORDER, null)
                ?: application.getString(de.psdev.devdrawer.R.string.pref_sort_order_default)
        ),
        themeSetting = ThemeSetting.valueOf(
            getString(PreferenceKeys.THEME, null)
                ?: application.getString(de.psdev.devdrawer.R.string.pref_theme_default)
        ),
        dynamicColorEnabled = getBoolean(
            PreferenceKeys.DYNAMIC_COLOR,
            application.resources.getBoolean(de.psdev.devdrawer.R.bool.pref_dynamic_color_default)
        ),
        analyticsOptIn = getBoolean(PreferenceKeys.ANALYTICS_OPT_IN, false)
    )

}

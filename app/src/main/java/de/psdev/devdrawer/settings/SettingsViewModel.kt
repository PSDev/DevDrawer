package de.psdev.devdrawer.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.config.RemoteConfigService
import de.psdev.devdrawer.receivers.UpdateReceiver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
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

    val persistedSettings = callbackFlow<Settings> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, _: String ->
            trySendBlocking(sharedPreferences.loadSettings(application))
        }
        send(sharedPreferences.loadSettings(application))
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)

    init {
        viewModelScope.launch {
            // TODO Convert to Flow
            val analyticsEnabled = remoteConfigService.getBoolean(TrackingService.CONFIG_KEY_ENABLED)
            settingsFlow().collect { settings ->
                viewState.value = ViewState.Loaded(
                    analyticsVisible = analyticsEnabled,
                    settings = settings
                )
            }
        }
    }

    fun onActivityChooserChanged(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(application.getString(R.string.pref_show_activity_choice), enabled)
        }
        onSettingsUpdated()
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        sharedPreferences.edit {
            putString(application.getString(R.string.pref_sort_order), sortOrder.name)
        }
        onSettingsUpdated()
    }

    fun onAnalyticsOptInChanged(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(application.getString(R.string.pref_feature_analytics_opted_in), enabled)
        }
        onSettingsUpdated()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun settingsFlow(): Flow<Settings> = callbackFlow<Settings> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
            logger.warn { "Setting updated: $key" }
            trySendBlocking(sharedPreferences.loadSettings(application))
        }
        send(sharedPreferences.loadSettings(application))
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun onSettingsUpdated() {
        UpdateReceiver.send(application)
    }

    sealed class ViewState {
        object Loading: ViewState()
        data class Loaded(
            val analyticsVisible: Boolean,
            val settings: Settings
        ): ViewState()
    }

    data class Settings(
        val activityChooserEnabled: Boolean,
        val defaultSortOrder: SortOrder,
        val analyticsOptIn: Boolean
    )

    private fun SharedPreferences.loadSettings(application: Application): Settings = Settings(
        activityChooserEnabled = getBoolean(
            application.getString(R.string.pref_show_activity_choice),
            application.resources.getBoolean(R.bool.pref_show_activity_choice_default)
        ),
        defaultSortOrder = SortOrder.valueOf(
            getString(application.resources.getString(R.string.pref_sort_order), null) ?: application.getString(R.string.pref_sort_order_default)
        ),
        analyticsOptIn = getBoolean(application.getString(R.string.pref_feature_analytics_opted_in), false)
    )

}
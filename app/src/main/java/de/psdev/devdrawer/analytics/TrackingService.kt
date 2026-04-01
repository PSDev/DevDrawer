package de.psdev.devdrawer.analytics

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import de.psdev.devdrawer.config.RemoteConfigService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mu.KLogging
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingService @Inject constructor(
    private val application: Application,
    private val remoteConfigService: RemoteConfigService,
) {
    companion object: KLogging() {
        const val PREF_KEY_OPTED_IN = "feature_analytics_opted_in"
        const val PREF_KEY_OPTED_IN_TIME = "feature_analytics_opted_in_time"
        const val CONFIG_KEY_ENABLED = "feature_analytics_enabled"
        const val CONFIG_KEY_MIN_TIME = "feature_analytics_optin_min_time"
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)
    }

    private val _needsOptIn = MutableStateFlow(false)
    val needsOptIn: StateFlow<Boolean> = _needsOptIn

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    private val preferenceChangedListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            logger.info { "Preference changed: $key" }
            when (key) {
                PREF_KEY_OPTED_IN -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    sharedPreferences.edit {
                        putLong(PREF_KEY_OPTED_IN_TIME, System.currentTimeMillis())
                    }
                    updateAnalyticsCollection(value)
                    _needsOptIn.value = false
                }
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
        updateAnalyticsCollection(sharedPreferences.getBoolean(PREF_KEY_OPTED_IN, false))
    }

    fun trackAction(name: String) {
        firebaseAnalytics.logEvent(name) {}
    }

    fun trackScreen(clazz: Class<*>, name: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, clazz.simpleName)
        }
    }

    suspend fun checkOptIn() {
        if (remoteConfigService.getBoolean(CONFIG_KEY_ENABLED)) {
            val optInTime = sharedPreferences.getLong(PREF_KEY_OPTED_IN_TIME, 0L)
            val minOptInTime = remoteConfigService.getLong(CONFIG_KEY_MIN_TIME)
            val optInTooOld = optInTime < minOptInTime

            val status = if (sharedPreferences.contains(PREF_KEY_OPTED_IN)) {
                if (sharedPreferences.getBoolean(PREF_KEY_OPTED_IN, false)) OptInStatus.OPT_IN else OptInStatus.OPT_OUT
            } else OptInStatus.UNKNOWN

            if (status == OptInStatus.UNKNOWN || (status == OptInStatus.OPT_IN && optInTooOld)) {
                _needsOptIn.value = true
            }
        } else {
            // Remote config has disabled analytics — ensure collection is off regardless of stored preference.
            updateAnalyticsCollection(false)
        }
    }

    fun optIn() {
        sharedPreferences.edit { putBoolean(PREF_KEY_OPTED_IN, true) }
    }

    fun optOut() {
        sharedPreferences.edit { putBoolean(PREF_KEY_OPTED_IN, false) }
    }

    private fun updateAnalyticsCollection(enabled: Boolean) {
        val consent = if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED
        firebaseAnalytics.setConsent(mapOf(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to consent))
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    enum class OptInStatus {
        UNKNOWN, OPT_IN, OPT_OUT
    }
}

package de.psdev.devdrawer.analytics

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.text.parseAsHtml
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import de.psdev.devdrawer.config.RemoteConfigService
import kotlinx.coroutines.*
import mu.KLogging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

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
        PreferenceManager.getDefaultSharedPreferences(
            application
        )
    }
    private var firebaseAnalyticsOptInStatus: OptInStatus
        get() = if (sharedPreferences.contains(PREF_KEY_OPTED_IN)) {
            when (sharedPreferences.getBoolean(PREF_KEY_OPTED_IN, false)) {
                true -> OptInStatus.OPT_IN
                false -> OptInStatus.OPT_OUT
            }
        } else OptInStatus.UNKNOWN
        set(value) = when (value) {
            OptInStatus.OPT_IN -> sharedPreferences.edit { putBoolean(PREF_KEY_OPTED_IN, true) }
            OptInStatus.OPT_OUT -> sharedPreferences.edit { putBoolean(PREF_KEY_OPTED_IN, false) }
            OptInStatus.UNKNOWN -> throw IllegalArgumentException("UNKNOWN not allowed")
        }
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
                    if (value) {
                        setConsentStatus(FirebaseAnalytics.ConsentStatus.GRANTED)
                        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
                    } else {
                        setConsentStatus(FirebaseAnalytics.ConsentStatus.DENIED)
                        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
                    }
                }
            }
        }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
    }

    fun trackAction(name: String) {
        firebaseAnalytics.logEvent(name) {

        }
    }

    fun trackScreen(clazz: Class<*>, name: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, clazz.simpleName)
        }
    }

    suspend fun checkOptIn(activity: Activity) {
        if (remoteConfigService.getBoolean(CONFIG_KEY_ENABLED)) {
            val optInTime = sharedPreferences.getLong(PREF_KEY_OPTED_IN_TIME, 0L)
            val minOptInTime = remoteConfigService.getLong(CONFIG_KEY_MIN_TIME)
            val optInTooOld = optInTime < minOptInTime
            val status = firebaseAnalyticsOptInStatus
            if (status == OptInStatus.UNKNOWN || (status == OptInStatus.OPT_IN && optInTooOld)) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val alertDialog = MaterialAlertDialogBuilder(activity)
                        .setTitle(buildSpannedString { bold { appendLine("Usage analytics") } })
                        .setMessage(
                            // If optInTooOld use different message
                            buildSpannedString {
                                italic { appendLine("Thank you for installing this app.") }.appendLine()
                                appendLine("In order for us to be able to better understand your use of the app we would like to analyse your usage.").appendLine()
                                bold { appendLine("What does this mean exactly?") }
                                appendLine("We use Firebase Analytics to track")
                                appendLine("* opened screens")
                                appendLine("* certain interactions (clicks) on elements").appendLine()
                                appendLine("We don't store any personally identifiably data and don't collect your advertising ID.").appendLine()
                                appendLine("Additionally we use Firebase Crashlytics to get automatic reports of app crashes.").appendLine()
                                append("If you have any question you can write us at ")
                                append("<a href=\"mailto:privacy@psdev.de\">privacy@psdev.de</a>".parseAsHtml())
                                appendLine(".")
                                appendLine("Please consider helping us by opting in.").appendLine()
                                bold { append("Thank you!") }
                            }
                        )
                        .setPositiveButton("Opt-in") { _, _ ->
                            optIn()
                            Snackbar.make(
                                activity.findViewById(android.R.id.content),
                                buildSpannedString {
                                    bold { append("Thank you! You can change your decision anytime on the settings tab.") }
                                },
                                Snackbar.LENGTH_LONG
                            ).apply {
                                animationMode = Snackbar.ANIMATION_MODE_SLIDE
                                setAction("OK") {
                                    dismiss()
                                }
                            }.show()
                        }
                        .setNegativeButton("Opt-out") { _, _ -> optOut() }
                        .setOnDismissListener { continuation.resume(Unit) }
                        .setCancelable(false)
                        .create()
                    alertDialog.setCanceledOnTouchOutside(false)
                    val job = coroutineScope.async(start = CoroutineStart.LAZY) {
                        alertDialog.findViewById<TextView>(android.R.id.message)?.movementMethod =
                            LinkMovementMethod.getInstance()
                        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        positiveButton.isEnabled = false
                        negativeButton.isEnabled = false
                        delay(2500)
                        positiveButton.isEnabled = true
                        negativeButton.isEnabled = true
                    }
                    continuation.invokeOnCancellation {
                        alertDialog.dismiss()
                        job.cancel()
                    }
                    alertDialog.setOnShowListener { job.start() }
                    alertDialog.show()
                }
            } else if (status == OptInStatus.OPT_IN) {
                firebaseAnalytics.setAnalyticsCollectionEnabled(true)
            }
        } else {
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        }
    }

    private fun optIn() {
        firebaseAnalyticsOptInStatus = OptInStatus.OPT_IN
    }

    private fun optOut() {
        firebaseAnalyticsOptInStatus = OptInStatus.OPT_OUT
    }

    private fun setConsentStatus(contentStatus: FirebaseAnalytics.ConsentStatus) {
        firebaseAnalytics.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to contentStatus
            )
        )
    }

    enum class OptInStatus {
        UNKNOWN, OPT_IN, OPT_OUT
    }

}
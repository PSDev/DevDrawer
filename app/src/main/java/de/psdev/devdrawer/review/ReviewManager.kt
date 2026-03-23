package de.psdev.devdrawer.review

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import de.psdev.devdrawer.config.RemoteConfigService
import de.psdev.devdrawer.database.DevDrawerDatabase
import mu.KLogging
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewManager @Inject constructor(
    private val application: Application,
    private val remoteConfigService: RemoteConfigService,
    private val devDrawerDatabase: DevDrawerDatabase,
    private val sharedPreferences: SharedPreferences
) {
    companion object : KLogging() {
        const val PREF_KEY_LAST_LAUNCH_MILLIS = "feature_inappreview_last_launch"
        const val KEY_ENABLED = "feature_inappreview_enabled"
        const val KEY_MIN_WIDGETS = "feature_inappreview_minwidgets"
        const val KEY_MIN_TIME_BETWEEN_LAUNCH = "feature_inappreview_mintimebetweenlaunch"
    }

    private val reviewManager by lazy { ReviewManagerFactory.create(application) }
    private var lastReviewLaunchMillis: Long
        get() = sharedPreferences.getLong(PREF_KEY_LAST_LAUNCH_MILLIS, 0L)
        set(value) = sharedPreferences.edit { putLong(PREF_KEY_LAST_LAUNCH_MILLIS, value) }

    suspend fun triggerReview(activity: Activity) {
        try {
            if (!remoteConfigService.getBoolean(KEY_ENABLED)) return
            if (shouldLaunchReview()) {
                logger.info { "Requesting review" }
                val reviewInfo = reviewManager.requestReview()
                reviewManager.launchReview(activity, reviewInfo)
                lastReviewLaunchMillis = System.currentTimeMillis()
            } else {
                logger.info { "Conditions not met, skipping review" }
            }
        } catch (e: RuntimeException) {
            logger.warn { "Error triggering review request: ${e.message}" }
        }
    }

    private suspend fun shouldLaunchReview(): Boolean {
        val servicesAvailable = GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(application)
        if (servicesAvailable != ConnectionResult.SUCCESS) {
            return false
        }
        val minWidgetsConfig = remoteConfigService.getInteger(KEY_MIN_WIDGETS)
        val currentWidgetCount = devDrawerDatabase.widgetDao().findAll().count()
        if (currentWidgetCount < minWidgetsConfig) {
            return false
        }
        val minTimeBetweenReviews = remoteConfigService.getLong(KEY_MIN_TIME_BETWEEN_LAUNCH)
        if ((System.currentTimeMillis() - lastReviewLaunchMillis) < minTimeBetweenReviews) {
            return false
        }

        return true
    }

}
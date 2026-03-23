package de.psdev.devdrawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.review.ReviewManager
import de.psdev.devdrawer.updates.UpdateManager
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var trackingService: TrackingService

    @Inject
    lateinit var reviewManager: ReviewManager

    @Inject
    lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            reviewManager.triggerReview(this@BaseActivity)
        }
        updateManager.checkForUpdates(this)
    }

    override fun onResume() {
        super.onResume()
        updateManager.resumeUpdate(this)
    }
}

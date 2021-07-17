package de.psdev.devdrawer

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import de.psdev.devdrawer.analytics.TrackingService
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var trackingService: TrackingService
    protected var toolbarTitle: CharSequence
        get() = requireActivity().title
        set(value) {
            requireActivity().title = value
        }
    val Fragment.viewLifecycleScope: LifecycleCoroutineScope
        get() = viewLifecycleOwner.lifecycleScope

    protected fun updateToolbarTitle(@StringRes resId: Int) {
        requireActivity().setTitle(resId)
        trackingService.trackScreen(this::class.java, getString(resId))
    }
}
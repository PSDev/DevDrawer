package de.psdev.devdrawer.receivers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A widget the launcher placed from the app's "Add widget" button. The launcher doesn't open setup for these,
 * so the app opens it as soon as it is back in front.
 */
object PinnedWidgets {
    private val pending = MutableStateFlow<Int?>(null)
    val pendingSetup: StateFlow<Int?> = pending.asStateFlow()

    fun widgetPinned(widgetId: Int) {
        pending.value = widgetId
    }

    fun setupOpened(widgetId: Int) {
        pending.compareAndSet(widgetId, null)
    }
}

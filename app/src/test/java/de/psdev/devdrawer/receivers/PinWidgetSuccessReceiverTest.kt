package de.psdev.devdrawer.receivers

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinWidgetSuccessReceiverTest {

    @After
    fun tearDown() {
        PinnedWidgets.pendingSetup.value?.let(PinnedWidgets::setupOpened)
    }

    @Test
    fun `given the launcher placed a widget, when it confirms, then that widget waits for setup`() {
        // Given
        val intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 12)

        // When
        PinWidgetSuccessReceiver().onReceive(ApplicationProvider.getApplicationContext(), intent)

        // Then
        assertEquals(12, PinnedWidgets.pendingSetup.value)
    }

    @Test
    fun `given a pinned widget waiting, when its setup opens, then it no longer waits`() {
        // Given
        PinnedWidgets.widgetPinned(12)

        // When
        PinnedWidgets.setupOpened(12)

        // Then
        assertNull(PinnedWidgets.pendingSetup.value)
    }

    @Test
    fun `given a confirmation without a widget id, when received, then nothing waits for setup`() {
        // Given / When
        PinWidgetSuccessReceiver().onReceive(ApplicationProvider.getApplicationContext(), Intent())

        // Then
        assertNull(PinnedWidgets.pendingSetup.value)
    }
}

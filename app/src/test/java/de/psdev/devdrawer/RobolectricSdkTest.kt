package de.psdev.devdrawer

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Guards robolectric.properties: the tests should run on a current Android, not an old fallback. */
@RunWith(RobolectricTestRunner::class)
class RobolectricSdkTest {

    @Test
    fun `given the test setup, when a Robolectric test runs, then it runs on Android 16`() {
        assertEquals(36, Build.VERSION.SDK_INT)
    }
}

package de.psdev.devdrawer

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33], qualifiers = "de")
class GermanTranslationTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `given a German locale, when reading cancel, then it is translated`() {
        // Given / When
        val result = context.getString(R.string.cancel)

        // Then
        assertEquals("Abbrechen", result)
    }

    @Test
    fun `given a German locale, when reading yes, then it is translated`() {
        // Given / When
        val result = context.getString(R.string.yes)

        // Then
        assertEquals("Ja", result)
    }
}

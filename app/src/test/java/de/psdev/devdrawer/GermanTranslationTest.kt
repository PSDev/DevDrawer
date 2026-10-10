package de.psdev.devdrawer

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de")
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
    fun `given a German locale, when reading undo, then it is translated`() {
        // Given / When
        val result = context.getString(R.string.undo)

        // Then
        assertEquals("Rückgängig", result)
    }
}

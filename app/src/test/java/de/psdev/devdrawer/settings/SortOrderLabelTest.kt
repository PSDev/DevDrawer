package de.psdev.devdrawer.settings

import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class SortOrderLabelTest {

    @Test
    fun `given each sort order, when labelling it, then the label names that order`() {
        // Given
        val labels = RuntimeEnvironment.getApplication().resources.getStringArray(R.array.sort_order_labels)

        // When
        val result = SortOrder.entries.associateWith { it.label(labels) }

        // Then
        assertEquals(
            mapOf(
                SortOrder.FIRST_INSTALLED to "First installed",
                SortOrder.LAST_UPDATED to "Last updated",
                SortOrder.NAME to "Name",
                SortOrder.PACKAGE_NAME to "Package name"
            ),
            result
        )
    }
}

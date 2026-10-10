package de.psdev.devdrawer.appwidget

import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.fakes.FakeAppsService
import de.psdev.devdrawer.fakes.FakePackageFilterRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetContentLoaderTest {

    private val zeta = PackageHashInfo("com.example.zeta", firstInstallTime = 1, lastUpdateTime = 30, signatureHashSha256 = "key")
    private val alpha = PackageHashInfo("com.example.alpha", firstInstallTime = 2, lastUpdateTime = 10, signatureHashSha256 = "key")
    private val unrelated = PackageHashInfo("org.other", firstInstallTime = 3, lastUpdateTime = 20, signatureHashSha256 = "other")
    private val appsService = FakeAppsService(mapOf(zeta to "Zeta", alpha to "Alpha", unrelated to "Unrelated"))
    private val filters = FakePackageFilterRepository(
        listOf(PackageFilter(type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p1"))
    )

    private fun loader(widget: Widget?, defaultSortOrder: SortOrder = SortOrder.LAST_UPDATED) = WidgetContentLoader(
        widgetRepository = FakeWidgetRepository(listOfNotNull(widget)),
        packageFilterRepository = filters,
        appsService = appsService,
        defaultSortOrder = { defaultSortOrder }
    )

    @Test
    fun `given a widget without its own sort order, when loading, then matching apps use the default order`() = runTest {
        // Given
        val widget = Widget(id = 1, name = "Work", color = 0, profileId = "p1")

        // When
        val content = loader(widget, defaultSortOrder = SortOrder.LAST_UPDATED).load(1)

        // Then
        assertEquals(widget, content?.widget)
        assertEquals(listOf("Zeta", "Alpha"), content?.apps?.map { it.name })
    }

    @Test
    fun `given a widget with its own sort order, when loading, then it overrides the default`() = runTest {
        // Given
        val widget = Widget(id = 1, name = "Work", color = 0, profileId = "p1", sortOrder = SortOrder.NAME)

        // When
        val content = loader(widget, defaultSortOrder = SortOrder.LAST_UPDATED).load(1)

        // Then
        assertEquals(listOf("Alpha", "Zeta"), content?.apps?.map { it.name })
    }

    @Test
    fun `given no widget with that id, when loading, then there is no content`() = runTest {
        // Given / When
        val content = loader(widget = null).load(1)

        // Then
        assertNull(content)
    }
}

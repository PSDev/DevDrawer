package de.psdev.devdrawer.apps

import android.graphics.drawable.ColorDrawable
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class AppMatchingTest {

    private val devDrawer = PackageHashInfo("de.psdev.devdrawer", firstInstallTime = 1, lastUpdateTime = 30, signatureHashSha256 = "key-a")
    private val other = PackageHashInfo("com.example.other", firstInstallTime = 2, lastUpdateTime = 10, signatureHashSha256 = "key-a")
    private val client = PackageHashInfo("com.example.client", firstInstallTime = 3, lastUpdateTime = 20, signatureHashSha256 = "key-b")
    private val packages = listOf(devDrawer, other, client)

    @Test
    fun `given a signature filter, when matching, then every package with that signature matches`() {
        // Given
        val filters = listOf(PackageFilter(type = FilterType.SIGNATURE, filter = "key-a", profileId = "p"))

        // When
        val result = packages.matching(filters)

        // Then
        assertEquals(listOf(devDrawer, other), result)
    }

    @Test
    fun `given two filters, when matching, then packages matching either appear once`() {
        // Given
        val filters = listOf(
            PackageFilter(type = FilterType.SIGNATURE, filter = "key-a", profileId = "p"),
            PackageFilter(type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p")
        )

        // When
        val result = packages.matching(filters)

        // Then
        assertEquals(listOf(devDrawer, other, client), result)
    }

    @Test
    fun `given no filters, when matching, then nothing matches`() {
        // Given / When
        val result = packages.matching(emptyList())

        // Then
        assertEquals(emptyList<PackageHashInfo>(), result)
    }

    @Test
    fun `given each sort order, when sorting apps, then they are ordered accordingly`() {
        // Given
        val apps = listOf(
            app("DevDrawer2", devDrawer),
            app("Other", other),
            app("Client", client)
        )

        // When / Then
        assertEquals(listOf("DevDrawer2", "Client", "Other"), apps.sortedWith(SortOrder.LAST_UPDATED.comparator()).map { it.name })
        assertEquals(listOf("Client", "Other", "DevDrawer2"), apps.sortedWith(SortOrder.FIRST_INSTALLED.comparator()).map { it.name })
        assertEquals(listOf("Client", "DevDrawer2", "Other"), apps.sortedWith(SortOrder.NAME.comparator()).map { it.name })
        assertEquals(listOf("Client", "Other", "DevDrawer2"), apps.sortedWith(SortOrder.PACKAGE_NAME.comparator()).map { it.name })
    }

    private fun app(name: String, info: PackageHashInfo) = AppInfo(
        name = name,
        packageName = info.packageName,
        appIcon = ColorDrawable(0),
        firstInstallTime = info.firstInstallTime,
        lastUpdateTime = info.lastUpdateTime,
        signatureHashSha256 = info.signatureHashSha256
    )
}

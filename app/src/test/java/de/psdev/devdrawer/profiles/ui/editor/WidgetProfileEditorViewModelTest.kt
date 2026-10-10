package de.psdev.devdrawer.profiles.ui.editor

import androidx.room.Room
import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeAppsService
import de.psdev.devdrawer.profiles.PackageFilterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetProfileEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private lateinit var database: DevDrawerDatabase

    private val profile = WidgetProfile(id = "p1", name = "Mine")
    private val signatureFilter = PackageFilter(id = "f1", type = FilterType.SIGNATURE, filter = "key-a", description = "DevDrawer2", profileId = "p1")

    private val appsService = FakeAppsService(
        mapOf(
            PackageHashInfo("de.psdev.devdrawer", 1, 1, "key-a") to "DevDrawer2",
            PackageHashInfo("com.yourcompany.other", 1, 1, "key-a") to "Other",
            PackageHashInfo("com.example.client", 1, 1, "key-b") to "Client"
        )
    )

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), DevDrawerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.widgetProfileDao().insert(profile)
        database.packageFilterDao().insert(signatureFilter)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createViewModel(isNew: Boolean = false) = WidgetProfileEditorViewModel(
        profileId = "p1",
        isNew = isNew,
        database = database,
        packageFilterRepository = PackageFilterRepository(RuntimeEnvironment.getApplication(), database),
        appsService = appsService
    )

    @Test
    fun `given a saved signature filter, when loaded, then matching apps and the filter's count are shown`() = runTest {
        // Given
        val viewModel = createViewModel()

        // When (Room emits on its own threads, so wait for the loaded state)
        val state = viewModel.state.first { it.matchingApps.isNotEmpty() }

        // Then
        assertEquals(listOf("DevDrawer2", "Other"), state.matchingApps.map { it.name })
        assertEquals(mapOf("f1" to 2), state.filterAppCounts)
    }

    @Test
    fun `given a new filter, when added, then it is saved and its apps match`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.matchingApps.isNotEmpty() }

        // When
        viewModel.addPackageFilter(PackageFilter(id = "f2", type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p1"))
        val state = viewModel.state.first { it.matchingApps.size == 3 }

        // Then
        assertEquals(listOf("Client", "DevDrawer2", "Other"), state.matchingApps.map { it.name })
        assertEquals(mapOf("f1" to 2, "f2" to 1), state.filterAppCounts)
        assertEquals(setOf("f1", "f2"), database.packageFilterDao().findAllByProfile("p1").map { it.id }.toSet())
    }

    @Test
    fun `given a filter, when removed, then it is deleted and can be restored`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.matchingApps.isNotEmpty() }

        // When
        viewModel.deleteFilter(signatureFilter)
        val removed = viewModel.state.first { it.packageFilters.isEmpty() }

        // Then
        assertEquals(emptyList<String>(), removed.matchingApps.map { it.name })
        assertEquals(emptyList<PackageFilter>(), database.packageFilterDao().findAllByProfile("p1"))
        viewModel.restoreFilter(signatureFilter)
        viewModel.state.first { it.packageFilters == listOf(signatureFilter) }
    }

    @Test
    fun `given a new name, when typing pauses, then the profile is renamed`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null }

        // When
        viewModel.onNameChanged("Work apps")

        // Then
        assertEquals("Work apps", viewModel.state.first { it.widgetName == "Work apps" }.widgetName)
        viewModel.state.first { it.widgetProfile?.name == "Work apps" }
    }

    @Test
    fun `given a name still being typed, when leaving the editor, then it is saved immediately`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null }
        viewModel.onNameChanged("Work apps")

        // When
        viewModel.onEditorClosed()

        // Then
        viewModel.state.first { it.widgetProfile?.name == "Work apps" }
    }

    @Test
    fun `given a cleared name, when typing pauses, then the profile keeps its saved name`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null }

        // When
        viewModel.onNameChanged(" ")
        advanceUntilIdle()
        viewModel.addPackageFilter(PackageFilter(id = "f2", type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p1"))
        viewModel.state.first { it.packageFilters.size == 2 }

        // Then
        assertEquals("Mine", database.widgetProfileDao().findById("p1")?.name)
    }

    @Test
    fun `given a package pattern, when typed, then the preview counts and lists the apps it matches`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.patternPreview.collect {} }

        // When
        viewModel.onPatternChanged("com.*")
        val preview = viewModel.patternPreview.first { it.matchCount == 2 }

        // Then
        assertTrue(preview.isValid)
        assertEquals(listOf("Client", "Other"), preview.apps.map { it.name })
    }

    @Test
    fun `given an invalid package pattern, when typed, then the preview says so and matches nothing`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.patternPreview.collect {} }

        // When
        viewModel.onPatternChanged("com.(")
        val preview = viewModel.patternPreview.first { it.pattern == "com.(" }

        // Then
        assertFalse(preview.isValid)
        assertEquals(0, preview.matchCount)
    }

    @Test
    fun `given a new profile left untouched, when the editor closes, then the profile is discarded`() = runTest {
        // Given
        database.packageFilterDao().delete(signatureFilter)
        val viewModel = createViewModel(isNew = true)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null && it.packageFilters.isEmpty() }

        // When
        viewModel.onEditorClosed()

        // Then
        viewModel.state.first { it.widgetProfile == null }
        assertNull(database.widgetProfileDao().findById("p1"))
    }

    @Test
    fun `given a new profile with a filter, when the editor closes, then the profile is kept`() = runTest {
        // Given
        val viewModel = createViewModel(isNew = true)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.packageFilters.isNotEmpty() }

        // When
        viewModel.onEditorClosed()
        advanceUntilIdle()

        // Then
        assertEquals("Mine", database.widgetProfileDao().findById("p1")?.name)
    }

    @Test
    fun `given a new profile that was only renamed, when the editor closes, then the profile is kept`() = runTest {
        // Given
        database.packageFilterDao().delete(signatureFilter)
        val viewModel = createViewModel(isNew = true)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null && it.packageFilters.isEmpty() }
        viewModel.onNameChanged("Clients")

        // When
        viewModel.onEditorClosed()

        // Then
        viewModel.state.first { it.widgetProfile?.name == "Clients" }
    }

    @Test
    fun `given an existing empty profile, when the editor closes, then it is kept`() = runTest {
        // Given
        database.packageFilterDao().delete(signatureFilter)
        val viewModel = createViewModel(isNew = false)
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.state.first { it.widgetProfile != null && it.packageFilters.isEmpty() }

        // When
        viewModel.onEditorClosed()
        advanceUntilIdle()

        // Then
        assertEquals("Mine", database.widgetProfileDao().findById("p1")?.name)
    }

    @Test
    fun `given widgets using the profile, when loaded, then the editor names them`() = runTest {
        // Given
        database.widgetDao().insert(Widget(id = 1, name = "Work apps", color = 0, profileId = "p1"))
        database.widgetDao().insert(Widget(id = 2, name = "Clients", color = 0, profileId = "p1"))
        val viewModel = createViewModel()

        // When
        val state = viewModel.state.first { it.usedByWidgets.size == 2 }

        // Then
        assertEquals(listOf("Clients", "Work apps"), state.usedByWidgets)
    }
}

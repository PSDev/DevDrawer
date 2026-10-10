package de.psdev.devdrawer.profiles.ui.editor

import androidx.room.Room
import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
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

    private fun createViewModel() = WidgetProfileEditorViewModel(
        profileId = "p1",
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
        viewModel.flushPendingChanges()

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
}

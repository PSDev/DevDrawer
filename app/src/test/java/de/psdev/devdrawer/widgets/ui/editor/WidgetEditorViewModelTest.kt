package de.psdev.devdrawer.widgets.ui.editor

import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeAppsService
import de.psdev.devdrawer.fakes.FakePackageFilterRepository
import de.psdev.devdrawer.fakes.FakeSortOrderSettings
import de.psdev.devdrawer.fakes.FakeWidgetProfileRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = WidgetProfile(id = "profile-1", name = "My Profile")
    private val other = WidgetProfile(id = "profile-2", name = "Other")
    private val widget = Widget(id = 1, name = "Test Widget", color = 0, profileId = "profile-1")
    private val zeta = PackageHashInfo("com.example.zeta", firstInstallTime = 1, lastUpdateTime = 30, signatureHashSha256 = "k")
    private val alpha = PackageHashInfo("com.example.alpha", firstInstallTime = 2, lastUpdateTime = 10, signatureHashSha256 = "k")

    private fun createViewModel(widgets: List<Widget> = listOf(widget)) = WidgetEditorViewModel(
        widgetId = 1,
        widgetRepository = FakeWidgetRepository(widgets),
        widgetProfileRepository = FakeWidgetProfileRepository(listOf(profile, other)),
        packageFilterRepository = FakePackageFilterRepository(
            listOf(PackageFilter(type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "profile-1"))
        ),
        appsService = FakeAppsService(mapOf(zeta to "Zeta", alpha to "Alpha")),
        sortOrderSettings = FakeSortOrderSettings(SortOrder.LAST_UPDATED)
    )

    @Test
    fun `given a new view model, when no coroutines have run, then state is Empty`() {
        // Given / When
        val viewModel = createViewModel()

        // Then
        assertEquals(WidgetEditorViewState.Empty, viewModel.state.value)
    }

    @Test
    fun `given a widget and profiles, when loaded, then profiles carry app counts and the preview shows matching apps`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(widget, state.persistedWidget)
        assertEquals(listOf("My Profile" to 2, "Other" to 0), state.profiles.map { it.profile.name to it.appCount })
        assertEquals(listOf("Zeta", "Alpha"), state.previewApps.map { it.name })
        assertEquals(SortOrder.LAST_UPDATED, state.defaultSortOrder)
    }

    @Test
    fun `given a loaded widget, when a header colour is picked, then it is saved right away`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onHeaderColorSelected(WidgetHeaderColor.DARK)
        advanceUntilIdle()

        // Then
        assertEquals(WidgetHeaderColor.DARK, viewModel.state.value.editableWidget?.headerColor)
        assertEquals(WidgetHeaderColor.DARK, viewModel.state.value.persistedWidget?.headerColor)
    }

    @Test
    fun `given a loaded widget, when the name is typed, then it is saved once typing pauses`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onNameChanged("Renamed")
        advanceTimeBy(100)
        runCurrent()

        // Then
        assertEquals("Renamed", viewModel.state.value.editableWidget?.name)
        assertEquals("Test Widget", viewModel.state.value.persistedWidget?.name)
        advanceUntilIdle()
        assertEquals("Renamed", viewModel.state.value.persistedWidget?.name)
    }

    @Test
    fun `given a name still being typed, when leaving the editor, then it is saved immediately`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onNameChanged("Renamed")

        // When
        viewModel.flushPendingChanges()
        runCurrent()

        // Then
        assertEquals("Renamed", viewModel.state.value.persistedWidget?.name)
    }

    @Test
    fun `given a cleared name, when typing pauses, then the widget keeps its saved name`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onNameChanged("  ")
        advanceUntilIdle()

        // Then
        assertEquals("Test Widget", viewModel.state.value.persistedWidget?.name)
    }

    @Test
    fun `given a loaded widget, when its own sort order is picked, then the preview follows it`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onSortOrderSelected(SortOrder.NAME)
        advanceUntilIdle()

        // Then
        assertEquals(SortOrder.NAME, viewModel.state.value.editableWidget?.sortOrder)
        assertEquals(listOf("Alpha", "Zeta"), viewModel.state.value.previewApps.map { it.name })
    }

    @Test
    fun `given a loaded widget, when another profile is selected, then it is saved and the preview follows it`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.onWidgetProfileSelected(other)
        advanceUntilIdle()

        // Then
        assertEquals("profile-2", viewModel.state.value.editableWidget?.profileId)
        assertEquals("profile-2", viewModel.state.value.persistedWidget?.profileId)
        assertEquals(emptyList<String>(), viewModel.state.value.previewApps.map { it.name })
    }
}

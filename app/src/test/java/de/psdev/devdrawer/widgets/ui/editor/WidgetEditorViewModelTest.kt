package de.psdev.devdrawer.widgets.ui.editor

import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeWidgetProfileRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = WidgetProfile(id = "profile-1", name = "My Profile")
    private val widget = Widget(id = 1, name = "Test Widget", color = 0xFF0000, profileId = "profile-1")

    private fun createViewModel(
        widgets: List<Widget> = listOf(widget),
        profiles: List<WidgetProfile> = listOf(profile)
    ) = WidgetEditorViewModel(
        widgetId = 1,
        widgetRepository = FakeWidgetRepository(widgets),
        widgetProfileRepository = FakeWidgetProfileRepository(profiles),
        trackingService = mockk(relaxed = true)
    )

    @Test
    fun `given a new view model, when no coroutines have run, then state is Empty`() {
        // Given / When
        val viewModel = createViewModel()

        // Then
        assertEquals(WidgetEditorViewState.Empty, viewModel.state.value)
    }

    @Test
    fun `given a widget and profile exist, when the state is collected, then both are emitted`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        // When
        advanceUntilIdle()

        // Then
        assertEquals(widget, viewModel.state.value.persistedWidget)
        assertEquals(widget, viewModel.state.value.editableWidget)
        assertEquals(listOf(profile), viewModel.state.value.widgetProfiles)
    }

    @Test
    fun `given a loaded widget, when the name is changed, then editable name updates without affecting persisted name`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onNameChanged("Renamed")
            advanceUntilIdle()

            // Then
            assertEquals("Renamed", viewModel.state.value.editableWidget?.name)
            assertEquals("Test Widget", viewModel.state.value.persistedWidget?.name)
        }

    @Test
    fun `given a loaded widget, when the color is changed, then editable color updates without affecting persisted color`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onWidgetColorChanged(0x0000FF)
            advanceUntilIdle()

            // Then
            assertEquals(0x0000FF, viewModel.state.value.editableWidget?.color)
            assertEquals(0xFF0000, viewModel.state.value.persistedWidget?.color)
        }

    @Test
    fun `given a loaded widget, when a new profile is selected, then editable profileId updates without affecting persisted profileId`() =
        runTest {
            // Given
            val other = WidgetProfile(id = "profile-2", name = "Other")
            val viewModel = createViewModel(profiles = listOf(profile, other))
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onWidgetProfileSelected(other)
            advanceUntilIdle()

            // Then
            assertEquals("profile-2", viewModel.state.value.editableWidget?.profileId)
            assertEquals("profile-1", viewModel.state.value.persistedWidget?.profileId)
        }

    @Test
    fun `given a renamed widget, when changes are saved, then the persisted widget reflects the new name`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onNameChanged("Saved Name")

        // When
        viewModel.saveChanges()
        advanceUntilIdle()

        // Then
        assertEquals("Saved Name", viewModel.state.value.persistedWidget?.name)
    }

    @Test
    fun `given a loaded widget, when the widget is deleted, then the persisted widget becomes null`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        // When
        viewModel.deleteWidget(widget)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.state.value.persistedWidget)
    }
}

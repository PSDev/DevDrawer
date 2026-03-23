package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeWidgetProfileRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import de.psdev.devdrawer.ui.UiState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetProfilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile1 = WidgetProfile(id = "p1", name = "Profile 1")
    private val profile2 = WidgetProfile(id = "p2", name = "Profile 2")

    private fun createViewModel(
        profiles: List<WidgetProfile> = listOf(profile1, profile2),
        widgets: List<Widget> = emptyList()
    ) = WidgetProfilesViewModel(
        widgetProfileRepository = FakeWidgetProfileRepository(profiles),
        widgetRepository = FakeWidgetRepository(widgets),
        trackingService = mockk(relaxed = true)
    )

    @Test
    fun `given a new view model, when no coroutines have run, then viewState is Loading`() {
        // Given / When
        val viewModel = createViewModel()

        // Then
        assertEquals(UiState.Loading, viewModel.viewState.value)
    }

    @Test
    fun `given profiles exist, when viewState is collected, then profiles are emitted`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.viewState.collect {} }

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.viewState.value
        assertTrue(state is UiState.Success)
        assertEquals(listOf(profile1, profile2), (state as UiState.Success).data)
    }

    @Test
    fun `given two profiles, when one is deleted, then only the other remains in viewState`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        viewModel.deleteProfile(profile1)
        advanceUntilIdle()

        // Then
        val state = viewModel.viewState.value as UiState.Success
        assertEquals(listOf(profile2), state.data)
    }

    @Test
    fun `given no profiles, when a new profile is created, then it is added and the callback is invoked`() = runTest {
        // Given
        val viewModel = createViewModel(profiles = emptyList())
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        var created: WidgetProfile? = null
        viewModel.createNewProfile { created = it }
        advanceUntilIdle()

        // Then
        assertNotNull(created)
        assertEquals("Profile 1", created?.name)
        assertEquals(1, (viewModel.viewState.value as UiState.Success).data.size)
    }

    @Test
    fun `given two existing profiles, when a new profile is created, then it is named Profile 3`() = runTest {
        // Given
        val viewModel = createViewModel(profiles = listOf(profile1, profile2))
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        var created: WidgetProfile? = null
        viewModel.createNewProfile { created = it }
        advanceUntilIdle()

        // Then
        assertEquals("Profile 3", created?.name)
    }

    @Test
    fun `given a profile with no widgets, when deletion is prepared, then Showing state is returned`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        var result: DeleteDialogState? = null
        viewModel.prepareProfileDeletion(profile1) { result = it }
        advanceUntilIdle()

        // Then
        assertTrue(result is DeleteDialogState.Showing)
        assertEquals(profile1, (result as DeleteDialogState.Showing).widgetProfile)
    }

    @Test
    fun `given a profile that is in use by widgets, when deletion is prepared, then InUseError state is returned`() =
        runTest {
            // Given
            val widget = Widget(id = 1, name = "Home Widget", color = 0, profileId = "p1")
            val viewModel = createViewModel(widgets = listOf(widget))
            advanceUntilIdle()

            // When
            var result: DeleteDialogState? = null
            viewModel.prepareProfileDeletion(profile1) { result = it }
            advanceUntilIdle()

            // Then
            assertTrue(result is DeleteDialogState.InUseError)
            val error = result as DeleteDialogState.InUseError
            assertEquals(profile1, error.widgetProfile)
            assertEquals(listOf(widget), error.widgets)
        }
}

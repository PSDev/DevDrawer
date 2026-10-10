package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeAppsService
import de.psdev.devdrawer.fakes.FakePackageFilterRepository
import de.psdev.devdrawer.fakes.FakeWidgetProfileRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.widgets.IWidgetRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetProfilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile1 = WidgetProfile(id = "p1", name = "Profile 1")
    private val profile2 = WidgetProfile(id = "p2", name = "Profile 2")
    private val filter1 = PackageFilter(id = "f1", type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p1")

    private lateinit var profileRepository: FakeWidgetProfileRepository
    private lateinit var filterRepository: FakePackageFilterRepository

    private fun createViewModel(
        profiles: List<WidgetProfile> = listOf(profile1, profile2),
        widgets: List<Widget> = emptyList(),
        widgetRepository: IWidgetRepository = FakeWidgetRepository(widgets)
    ): WidgetProfilesViewModel {
        profileRepository = FakeWidgetProfileRepository(profiles)
        filterRepository = FakePackageFilterRepository(listOf(filter1))
        return WidgetProfilesViewModel(
            application = RuntimeEnvironment.getApplication(),
            widgetProfileRepository = profileRepository,
            widgetRepository = widgetRepository,
            packageFilterRepository = filterRepository,
            appsService = FakeAppsService(
                mapOf(
                    PackageHashInfo("com.example.a", 1, 1, "k") to "A",
                    PackageHashInfo("com.example.b", 1, 1, "k") to "B"
                )
            ),
            trackingService = mockk(relaxed = true)
        )
    }

    private fun WidgetProfilesViewModel.summaries() = (viewState.value as UiState.Success).data

    @Test
    fun `given a new view model, when no coroutines have run, then viewState is Loading`() {
        // Given / When
        val viewModel = createViewModel()

        // Then
        assertEquals(UiState.Loading, viewModel.viewState.value)
    }

    @Test
    fun `given profiles, filters and widgets, when loaded, then each summary has counts and its widgets`() = runTest {
        // Given
        val widget = Widget(id = 1, name = "Work apps", color = 0, profileId = "p1")
        val viewModel = createViewModel(widgets = listOf(widget))
        backgroundScope.launch { viewModel.viewState.collect {} }

        // When
        advanceUntilIdle()

        // Then
        val summaries = viewModel.summaries()
        assertEquals(listOf(profile1, profile2), summaries.map { it.profile })
        assertEquals(listOf(1, 0), summaries.map { it.filterCount })
        assertEquals(listOf(2, 0), summaries.map { it.appCount })
        assertEquals(listOf(listOf("Work apps"), emptyList()), summaries.map { it.usedBy })
    }

    @Test
    fun `given an unused profile, when deleted, then it is gone and undo restores it with its filters`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        var result: DeleteResult? = null
        viewModel.deleteProfile(profile1) { result = it }
        advanceUntilIdle()

        // Then
        assertTrue(result is DeleteResult.Deleted)
        assertEquals(listOf(profile2), viewModel.summaries().map { it.profile })

        // When
        viewModel.undoDelete((result as DeleteResult.Deleted))
        advanceUntilIdle()

        // Then
        assertEquals(setOf(profile1, profile2), viewModel.summaries().map { it.profile }.toSet())
        assertEquals(listOf(filter1), filterRepository.findAllByProfile("p1"))
    }

    @Test
    fun `given a profile in use by widgets, when deleting, then it stays and the widgets are reported`() = runTest {
        // Given
        val widget = Widget(id = 1, name = "Home Widget", color = 0, profileId = "p1")
        val viewModel = createViewModel(widgets = listOf(widget))
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        var result: DeleteResult? = null
        viewModel.deleteProfile(profile1) { result = it }
        advanceUntilIdle()

        // Then
        assertEquals(DeleteResult.InUse(profile1, listOf(widget)), result)
        assertEquals(listOf(profile1, profile2), viewModel.summaries().map { it.profile })
    }

    @Test
    fun `given a profile with filters, when duplicated, then a copy with copied filters is added`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        viewModel.duplicateProfile(profile1)
        advanceUntilIdle()

        // Then
        val copy = profileRepository.findAll().single { it.id !in setOf("p1", "p2") }
        assertEquals("Profile 1 (copy)", copy.name)
        val copiedFilter = filterRepository.findAllByProfile(copy.id).single()
        assertEquals("com.example.*", copiedFilter.filter)
        assertNotEquals(filter1.id, copiedFilter.id)
    }

    @Test
    fun `given two existing profiles, when a new profile is created, then it is named Profile 3 and reported`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // When
        var created: WidgetProfile? = null
        viewModel.createNewProfile { created = it }
        advanceUntilIdle()

        // Then
        assertNotNull(created)
        assertEquals("Profile 3", created?.name)
        assertEquals(3, viewModel.summaries().size)
    }

    @Test
    fun `given loading fails, when observed, then the error is reported instead of loading forever`() = runTest {
        // Given
        val failing = object : IWidgetRepository by FakeWidgetRepository() {
            override fun widgetsFlow(): Flow<List<Widget>> = flow { throw IllegalStateException("database closed") }
        }
        val viewModel = createViewModel(widgetRepository = failing)

        // When
        backgroundScope.launch { viewModel.viewState.collect {} }
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.viewState.value is UiState.Error)
    }
}

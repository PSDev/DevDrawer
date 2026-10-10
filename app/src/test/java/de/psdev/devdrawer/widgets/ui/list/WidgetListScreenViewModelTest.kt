package de.psdev.devdrawer.widgets.ui.list

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetListScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given widgets with profiles, when loaded, then each summary shows its profile and app count`() = runTest {
        // Given
        val work = WidgetProfile(id = "work", name = "Work apps")
        val empty = WidgetProfile(id = "empty", name = "Default")
        val viewModel = WidgetListScreenViewModel(
            application = RuntimeEnvironment.getApplication(),
            widgetRepository = FakeWidgetRepository(
                listOf(
                    Widget(id = 1, name = "Work", color = 0, profileId = "work"),
                    Widget(id = 2, name = "Widget 2", color = 0, profileId = "empty")
                )
            ),
            widgetProfileRepository = FakeWidgetProfileRepository(listOf(work, empty)),
            packageFilterRepository = FakePackageFilterRepository(
                listOf(PackageFilter(type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "work"))
            ),
            appsService = FakeAppsService(
                mapOf(
                    PackageHashInfo("com.example.a", 1, 1, "k") to "A",
                    PackageHashInfo("com.example.b", 1, 1, "k") to "B",
                    PackageHashInfo("org.other", 1, 1, "k") to "Other"
                )
            )
        )
        backgroundScope.launch { viewModel.state.collect {} }

        // When
        advanceUntilIdle()

        // Then
        val loaded = viewModel.state.value as WidgetListScreenState.Loaded
        assertEquals(listOf("Work apps" to 2, "Default" to 0), loaded.widgets.map { it.profileName to it.appCount })
    }
}

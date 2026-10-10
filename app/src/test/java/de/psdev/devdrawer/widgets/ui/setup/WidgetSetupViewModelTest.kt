package de.psdev.devdrawer.widgets.ui.setup

import de.psdev.devdrawer.MainDispatcherRule
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.fakes.FakeAppsService
import de.psdev.devdrawer.fakes.FakePackageFilterRepository
import de.psdev.devdrawer.fakes.FakeWidgetProfileRepository
import de.psdev.devdrawer.fakes.FakeWidgetRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
class WidgetSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val devDrawer = PackageHashInfo("de.psdev.devdrawer", firstInstallTime = 1, lastUpdateTime = 30, signatureHashSha256 = "key-a")
    private val other = PackageHashInfo("com.yourcompany.other", firstInstallTime = 2, lastUpdateTime = 10, signatureHashSha256 = "key-a")
    private val client = PackageHashInfo("com.example.client", firstInstallTime = 3, lastUpdateTime = 20, signatureHashSha256 = "key-b")
    private val system = PackageHashInfo("com.android.settings", firstInstallTime = 0, lastUpdateTime = 40, signatureHashSha256 = "platform")

    private val appsService = FakeAppsService(
        apps = mapOf(devDrawer to "DevDrawer2", other to "Other", client to "Client", system to "Settings"),
        systemPackages = setOf(system.packageName)
    )
    private val existingProfile = WidgetProfile(id = "existing", name = "Clients")
    private val widgetRepository = FakeWidgetRepository()
    private val profileRepository = FakeWidgetProfileRepository(listOf(existingProfile))
    private val filterRepository = FakePackageFilterRepository(
        listOf(PackageFilter(type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "existing"))
    )

    private fun createViewModel() = WidgetSetupViewModel(
        widgetId = 5,
        application = RuntimeEnvironment.getApplication(),
        widgetRepository = widgetRepository,
        widgetProfileRepository = profileRepository,
        packageFilterRepository = filterRepository,
        appsService = appsService,
        trackingService = mockk(relaxed = true)
    )

    @Test
    fun `given installed apps, when loaded, then your apps are listed newest first with their key mates and no system apps`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("DevDrawer2", "Client", "Other"), state.apps.map { it.name })
        assertEquals(1, state.apps.first { it.name == "DevDrawer2" }.otherAppsWithSameKey)
        assertEquals(0, state.apps.first { it.name == "Client" }.otherAppsWithSameKey)
        assertEquals(listOf("Clients"), state.profiles.map { it.profile.name })
        assertEquals(1, state.profiles.single().appCount)
    }

    @Test
    fun `given nothing chosen, when an app is picked, then the count covers every app with its key`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canFinish)

        // When
        viewModel.onAppSelected(devDrawer.packageName)
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.state.value.matchCount)
        assertTrue(viewModel.state.value.canFinish)
    }

    @Test
    fun `given a picked app, when finishing, then a signature profile is created and the new widget uses it`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onAppSelected(devDrawer.packageName)
        advanceUntilIdle()

        // When
        var done: Widget? = null
        viewModel.finish { done = it }
        advanceUntilIdle()

        // Then
        val profile = profileRepository.findAll().single { it.id != existingProfile.id }
        assertEquals("Signed like DevDrawer2", profile.name)
        val filter = filterRepository.findAllByProfile(profile.id).single()
        assertEquals(FilterType.SIGNATURE, filter.type)
        assertEquals("key-a", filter.filter)
        assertEquals("DevDrawer2", filter.description)
        val widget = widgetRepository.findById(5)!!
        assertEquals(profile.id, widget.profileId)
        assertEquals("Signed like DevDrawer2", widget.name)
        assertEquals(widget, done)
    }

    @Test
    fun `given a picked app, when Done is tapped twice quickly, then only one profile is created`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onAppSelected(devDrawer.packageName)
        advanceUntilIdle()

        // When
        var doneCount = 0
        viewModel.finish { doneCount++ }
        viewModel.finish { doneCount++ }
        advanceUntilIdle()

        // Then
        assertEquals(2, profileRepository.findAll().size)
        assertEquals(1, doneCount)
    }

    @Test
    fun `given a package pattern, when finishing, then a pattern profile is created`() = runTest {
        // Given
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onSourceSelected(SetupSource.PATTERN)
        viewModel.onPatternChanged("com.yourcompany.*")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.matchCount)

        // When
        viewModel.finish {}
        advanceUntilIdle()

        // Then
        val profile = profileRepository.findAll().single { it.id != existingProfile.id }
        assertEquals("com.yourcompany.*", profile.name)
        val filter = filterRepository.findAllByProfile(profile.id).single()
        assertEquals(FilterType.PACKAGE_NAME, filter.type)
        assertEquals("com.yourcompany.*", filter.filter)
    }

    @Test
    fun `given an existing widget, when reusing a profile, then the widget is reassigned and keeps its colour`() = runTest {
        // Given
        widgetRepository.save(Widget(id = 5, name = "Old", color = 0, profileId = "existing", headerColor = WidgetHeaderColor.DARK))
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onSourceSelected(SetupSource.PROFILE)
        viewModel.onProfileSelected(existingProfile.id)
        advanceUntilIdle()

        // When
        viewModel.finish {}
        advanceUntilIdle()

        // Then
        assertEquals(listOf(existingProfile), profileRepository.findAll())
        val widget = widgetRepository.findById(5)!!
        assertEquals("existing", widget.profileId)
        assertEquals("Clients", widget.name)
        assertEquals(WidgetHeaderColor.DARK, widget.headerColor)
    }
}

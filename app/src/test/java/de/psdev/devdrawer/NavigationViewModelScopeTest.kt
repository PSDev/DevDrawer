package de.psdev.devdrawer

import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Serializable
data class TestDetailRoute(val id: String) : NavKey

class TestDetailViewModel(val id: String) : ViewModel() {
    var cleared = false
    override fun onCleared() {
        cleared = true
    }
}

/** Each screen on the back stack must get its own view models, cleared when the screen is popped. */
@RunWith(RobolectricTestRunner::class)
class NavigationViewModelScopeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigator: Navigator
    private val created = mutableListOf<TestDetailViewModel>()

    private fun showApp() {
        composeTestRule.setContent {
            val state = rememberNavigationState(startRoute = WidgetListRoute, topLevelRoutes = setOf(WidgetListRoute, WidgetProfilesRoute))
            navigator = remember { Navigator(state) }
            NavDisplay(
                entries = state.toEntries(
                    entryProvider {
                        entry<WidgetListRoute> { Text("widgets") }
                        entry<WidgetProfilesRoute> { Text("profiles") }
                        entry<TestDetailRoute> { key ->
                            val viewModel = viewModel { TestDetailViewModel(key.id).also { created += it } }
                            Text("detail ${viewModel.id}")
                        }
                    }
                ),
                onBack = { navigator.goBack() }
            )
        }
    }

    @Test
    fun `given one detail screen was closed, when another opens, then it gets its own view model`() {
        // Given
        showApp()
        composeTestRule.runOnIdle { navigator.navigate(TestDetailRoute("a")) }
        composeTestRule.onNodeWithText("detail a").assertExists()
        composeTestRule.runOnIdle { navigator.goBack() }

        // When
        composeTestRule.runOnIdle { navigator.navigate(TestDetailRoute("b")) }

        // Then
        composeTestRule.onNodeWithText("detail b").assertExists()
        assertTrue(created.first { it.id == "a" }.cleared)
    }

    @Test
    fun `given a detail screen, when switching tabs and back, then its view model survives`() {
        // Given
        showApp()
        composeTestRule.runOnIdle { navigator.navigate(WidgetProfilesRoute) }
        composeTestRule.runOnIdle { navigator.navigate(TestDetailRoute("a")) }
        composeTestRule.onNodeWithText("detail a").assertExists()

        // When
        composeTestRule.runOnIdle { navigator.navigate(WidgetListRoute) }
        composeTestRule.onNodeWithText("widgets").assertExists()
        composeTestRule.runOnIdle { navigator.navigate(WidgetProfilesRoute) }

        // Then
        composeTestRule.onNodeWithText("detail a").assertExists()
        assertTrue(created.single().cleared.not())
    }
}

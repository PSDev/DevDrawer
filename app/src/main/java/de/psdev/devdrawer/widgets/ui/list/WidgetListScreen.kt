package de.psdev.devdrawer.widgets.ui.list

import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.*

@Composable
fun WidgetListScreen(
    navController: NavController,
    widgetListScreenViewModel: WidgetListScreenViewModel = hiltViewModel(),
    onWidgetClick: (Widget) -> Unit = {},
    onRequestPinWidgetClick: () -> Unit = {}
) {
    val state by widgetListScreenViewModel.widgets
        .onStart { delay(100L) }
        .map { WidgetListScreenState.Loaded(it) }
        .collectAsState(initial = WidgetListScreenState.Loading)
    WidgetListScreen(
        state = state,
        onWidgetClick = onWidgetClick,
        onRequestPinWidgetClick = onRequestPinWidgetClick
    )
}

@Composable
fun WidgetListScreen(
    state: WidgetListScreenState,
    onWidgetClick: (Widget) -> Unit = {},
    onRequestPinWidgetClick: () -> Unit = {}
) {
    when (state) {
        WidgetListScreenState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Text(text = stringResource(id = R.string.loading))
            }
        }
        is WidgetListScreenState.Loaded -> {
            val widgets = state.widgets
            if (widgets.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Text(
                        text = stringResource(id = R.string.no_widgets_created),
                        color = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Button(onClick = onRequestPinWidgetClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_outline_add_box_24),
                            contentDescription = "Add"
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(id = R.string.add_widget)
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    WidgetList(
                        widgets = widgets,
                        onWidgetClick = onWidgetClick,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    )
                    FloatingActionButton(
                        onClick = onRequestPinWidgetClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Pin new widget")
                    }
                }
            }
        }
    }
}

sealed class WidgetListScreenState {
    object Loading: WidgetListScreenState()
    data class Loaded(
        val widgets: List<Widget>
    ): WidgetListScreenState()
}

@Preview(name = "Loading", showSystemUi = true)
@Preview(name = "Loading (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen_Loading() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loading)
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loaded(testWidgets()))
    }
}

@Preview(name = "Empty", showSystemUi = true)
@Preview(name = "Empty (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen_Empty() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loaded(emptyList()))
    }
}

fun testWidgets(): List<Widget> = listOf(
    Widget(
        id = 1,
        name = "Test Widget",
        color = Color.BLACK,
        profileId = UUID.randomUUID().toString()
    ),
    Widget(
        id = 2,
        name = "Test Widget 2",
        color = Color.BLACK,
        profileId = UUID.randomUUID().toString()
    )
)

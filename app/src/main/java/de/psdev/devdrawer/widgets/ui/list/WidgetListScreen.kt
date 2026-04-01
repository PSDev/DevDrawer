package de.psdev.devdrawer.widgets.ui.list

import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import java.util.UUID

@Composable
fun WidgetListScreen(
    widgetListScreenViewModel: WidgetListScreenViewModel = hiltViewModel(),
    onWidgetClick: (Widget) -> Unit
) {
    val context = LocalContext.current
    val state by widgetListScreenViewModel.state.collectAsStateWithLifecycle()
    WidgetListScreen(
        state = state,
        onWidgetClick = onWidgetClick,
        onRequestPinWidgetClick = {
            widgetListScreenViewModel.requestAppWidgetPinning(context)
        }
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.isRequestPinAppWidgetSupported) {
                        Spacer(modifier = Modifier.size(16.dp))
                        Button(onClick = onRequestPinWidgetClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_outline_add_box_24),
                                contentDescription = stringResource(id = R.string.add_widget)
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = stringResource(id = R.string.add_widget)
                            )
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    WidgetList(
                        widgets = widgets,
                        onWidgetClick = onWidgetClick,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    )
                    if (state.isRequestPinAppWidgetSupported) {
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
}

sealed class WidgetListScreenState {
    data object Loading : WidgetListScreenState()
    data class Loaded(
        val widgets: List<Widget>,
        val isRequestPinAppWidgetSupported: Boolean = false
    ) : WidgetListScreenState()
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

@Preview(name = "Empty", showSystemUi = true)
@Preview(name = "Empty (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen_Empty_SupportsPinning() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loaded(emptyList(), true))
    }
}

@Preview(name = "Not empty", showSystemUi = true)
@Preview(name = "Not empty (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen_NotEmpty() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loaded(emptyList()))
    }
}

@Preview(name = "Not empty", showSystemUi = true)
@Preview(name = "Not empty (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetListScreen_NotEmpty_SupportsPinning() {
    DevDrawerTheme {
        WidgetListScreen(WidgetListScreenState.Loaded(emptyList(), true))
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

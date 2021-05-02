package de.psdev.devdrawer.widgets.ui.editor

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.utils.rememberFlowWithLifecycle
import java.util.*

@Composable
fun WidgetEditor(
    onEditWidgetProfile: (WidgetProfile) -> Unit = {},
    onChangesSaved: (Widget) -> Unit = {}
) {
    WidgetEditor(
        viewModel = hiltViewModel(),
        onEditWidgetProfile = onEditWidgetProfile,
        onChangesSaved = onChangesSaved
    )
}

@Composable
fun WidgetEditor(
    viewModel: WidgetEditorViewModel,
    onEditWidgetProfile: (WidgetProfile) -> Unit = {},
    onChangesSaved: (Widget) -> Unit = {}
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = WidgetEditorViewState.Empty)

    WidgetEditor(
        viewState = viewState,
        onNameChange = viewModel::onNameChanged,
        onColorSelected = { color ->
            viewModel.onWidgetColorChanged(color)
        },
        onEditWidgetProfile = onEditWidgetProfile,
        onWidgetProfileSelected = viewModel::onWidgetProfileSelected,
        onSaveChangesClick = {
            viewModel.saveChanges()
        }
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun WidgetEditor(
    viewState: WidgetEditorViewState,
    onNameChange: (String) -> Unit = {},
    onColorSelected: (Int) -> Unit = {},
    onEditWidgetProfile: (WidgetProfile) -> Unit = {},
    onWidgetProfileSelected: (WidgetProfile) -> Unit = {},
    onSaveChangesClick: () -> Unit = {}
) {
    val widget = viewState.editableWidget
    if (widget == null) {
        // Loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
    } else {
        var dialogState by remember { mutableStateOf<WidgetEditorDialogsState>(WidgetEditorDialogsState.None) }
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Surface(modifier = Modifier.wrapContentHeight(), elevation = 2.dp) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                value = widget.name,
                                onValueChange = onNameChange,
                                label = { Text(text = stringResource(id = R.string.name)) }
                            )
                            ColorBox(isSelectedColor = true, color = widget.color) {
                                dialogState = WidgetEditorDialogsState.ColorSelection(widget.color)
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewState.widgetProfiles) { widgetProfile ->
                        val backgroundColor by animateColorAsState(
                            targetValue = if (widget.profileId == widgetProfile.id) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                        )
                        Card(backgroundColor = backgroundColor, modifier = Modifier.combinedClickable(
                            onLongClick = { onEditWidgetProfile(widgetProfile) },
                            onClick = { onWidgetProfileSelected(widgetProfile) }
                        )) {
                            Row(
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(modifier = Modifier.weight(1f), text = widgetProfile.name)
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = viewState.persistedWidget != viewState.editableWidget,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onSaveChangesClick,
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Save, contentDescription = stringResource(id = R.string.save))
                }
            }
        }
        when (val state = dialogState) {
            WidgetEditorDialogsState.None -> Unit
            is WidgetEditorDialogsState.ColorSelection -> ColorSelectionDialog(
                initialColor = state.currentColor,
                onColorSelected = {
                    onColorSelected(it)
                    dialogState = WidgetEditorDialogsState.None
                },
                onDismiss = {
                    dialogState = WidgetEditorDialogsState.None
                }
            )
        }
    }
}

sealed class WidgetEditorDialogsState {
    object None: WidgetEditorDialogsState()
    data class ColorSelection(
        val currentColor: Int
    ): WidgetEditorDialogsState()
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetEditor_Loading() {
    DevDrawerTheme {
        WidgetEditor(
            viewState = WidgetEditorViewState.Empty
        )
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetEditor_Loaded() {
    val widgetProfile = WidgetProfile(
        id = UUID.randomUUID().toString(),
        name = "Test widget profile"
    )
    val widget = Widget(
        id = 1,
        name = "Test widget",
        color = android.graphics.Color.YELLOW,
        profileId = widgetProfile.id
    )
    DevDrawerTheme {
        WidgetEditor(
            viewState = WidgetEditorViewState(
                persistedWidget = widget,
                widgetProfiles = listOf(widgetProfile),
                editableWidget = widget
            )
        )
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetEditor_Loaded_Changed() {
    val widgetProfile = WidgetProfile(
        id = UUID.randomUUID().toString(),
        name = "Test widget profile"
    )
    val widgetProfile2 = WidgetProfile(
        id = UUID.randomUUID().toString(),
        name = "Test widget profile 2"
    )
    val widget = Widget(
        id = 1,
        name = "Test widget",
        color = android.graphics.Color.YELLOW,
        profileId = widgetProfile.id
    )
    DevDrawerTheme {
        WidgetEditor(
            viewState = WidgetEditorViewState(
                persistedWidget = widget,
                widgetProfiles = listOf(widgetProfile, widgetProfile2),
                editableWidget = widget.copy(profileId = widgetProfile2.id)
            )
        )
    }
}
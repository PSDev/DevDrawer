package de.psdev.devdrawer.widgets.ui.editor

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.psdev.devdrawer.AppBarActionsProvider
import de.psdev.devdrawer.ProvideMenu
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger { }

@Composable
fun WidgetEditorScreen(
    id: Int,
    viewModel: WidgetEditorViewModel = hiltViewModel<WidgetEditorViewModel, WidgetEditorViewModel.Factory> {
        it.create(
            id
        )
    },
    menuCallback: AppBarActionsProvider,
    onBack: () -> Unit,
    onEditWidgetProfile: (WidgetProfile) -> Unit
) {
    WidgetEditorScreen(
        viewModel = viewModel,
        menuCallback = menuCallback,
        onBack = onBack,
        onEditWidgetProfile = onEditWidgetProfile,
        onChangesSaved = {}
    )
}

@Composable
fun WidgetEditorScreen(
    viewModel: WidgetEditorViewModel,
    menuCallback: AppBarActionsProvider,
    onBack: () -> Unit,
    onEditWidgetProfile: (WidgetProfile) -> Unit,
    onChangesSaved: (Widget) -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    val persistedWidget = viewState.persistedWidget
    ProvideMenu(menuCallback, persistedWidget) {
        if (persistedWidget != null) {
            IconButton(onClick = {
                viewModel.deleteWidget(persistedWidget)
                onBack()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
    }
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
            persistedWidget?.let(onChangesSaved)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetEditor(
    modifier: Modifier = Modifier,
    viewState: WidgetEditorViewState,
    onNameChange: (String) -> Unit = {},
    onColorSelected: (Int) -> Unit = {},
    onEditWidgetProfile: (WidgetProfile) -> Unit = {},
    onWidgetProfileSelected: (WidgetProfile) -> Unit = {},
    onSaveChangesClick: () -> Unit = {}
) {
    val widget = viewState.editableWidget
    if (widget == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
    } else {
        var dialogState by remember {
            mutableStateOf<WidgetEditorDialogsState>(
                WidgetEditorDialogsState.None
            )
        }
        Box(modifier = modifier.fillMaxSize()) {
            Column {
                Surface(modifier = Modifier.wrapContentHeight(), shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(8.dp),
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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewState.widgetProfiles) { widgetProfile ->
                        Card(
                            modifier = Modifier.combinedClickable(
                                onLongClick = {
                                    onEditWidgetProfile(widgetProfile)
                                },
                                onClick = { onWidgetProfileSelected(widgetProfile) }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(modifier = Modifier.weight(1f), text = widgetProfile.name)
                                IconButton(onClick = {
                                    onEditWidgetProfile(widgetProfile)
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(id = R.string.edit_profile)
                                    )
                                }
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
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = stringResource(id = R.string.save)
                    )
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
    data object None : WidgetEditorDialogsState()
    data class ColorSelection(
        val currentColor: Int
    ) : WidgetEditorDialogsState()
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

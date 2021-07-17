package de.psdev.devdrawer.profiles.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.DeleteDialogState
import de.psdev.devdrawer.profiles.WidgetInUseErrorAlertDialog
import de.psdev.devdrawer.profiles.WidgetProfileList
import de.psdev.devdrawer.profiles.WidgetProfilesViewModel
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.launch

@Composable
fun WidgetProfilesScreen(
    viewModel: WidgetProfilesViewModel = hiltViewModel(),
    editProfile: (WidgetProfile) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var deleteDialogShown by remember { mutableStateOf<DeleteDialogState>(DeleteDialogState.Hidden) }
    val viewState by viewModel.viewState.collectAsState()
    WidgetProfileListScreen(
        viewState = viewState,
        onWidgetProfileClick = editProfile,
        onWidgetProfileLongClick = { widgetProfile ->
            coroutineScope.launch {
                val widgets = viewModel.devDrawerDatabase.widgetDao().findAllByProfileId(widgetProfile.id)
                deleteDialogShown = if (widgets.isNotEmpty()) {
                    DeleteDialogState.InUseError(
                        widgetProfile = widgetProfile,
                        widgets = widgets
                    )
                } else {
                    DeleteDialogState.Showing(widgetProfile)
                }
            }
        },
        onCreateWidgetProfileClick = {
            coroutineScope.launch {
                val widgetProfile = viewModel.createNewProfile()
                editProfile(widgetProfile)
            }
        }
    )
    when (val state = deleteDialogShown) {
        DeleteDialogState.Hidden -> Unit
        is DeleteDialogState.Showing -> AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = "Confirm")
            },
            text = {
                Text(text = "Do you really want to delete the profile '${state.widgetProfile.name}'?")
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        viewModel.deleteProfile(state.widgetProfile)
                        deleteDialogShown = DeleteDialogState.Hidden
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleteDialogShown = DeleteDialogState.Hidden
                }) {
                    Text("Cancel")
                }
            }
        )
        is DeleteDialogState.InUseError -> {
            WidgetInUseErrorAlertDialog(state, onDismiss = {
                deleteDialogShown = DeleteDialogState.Hidden
            })
        }
    }
}

@Composable
fun WidgetProfileListScreen(
    viewState: WidgetProfilesViewModel.ViewState,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onWidgetProfileLongClick: (WidgetProfile) -> Unit = {},
    onCreateWidgetProfileClick: () -> Unit = {}
) {
    when (viewState) {
        WidgetProfilesViewModel.ViewState.Loading -> LoadingView()
        is WidgetProfilesViewModel.ViewState.Loaded -> {
            val profiles = viewState.data
            if (profiles.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        color = MaterialTheme.colors.onBackground,
                        text = stringResource(id = R.string.no_profiles)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Button(onClick = onCreateWidgetProfileClick) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(id = R.string.widget_profile_list_create_new)
                        )
                        Text(text = stringResource(id = R.string.widget_profile_list_create_new))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    WidgetProfileList(
                        widgetProfiles = profiles,
                        onWidgetProfileClick = onWidgetProfileClick,
                        onWidgetProfileLongClick = onWidgetProfileLongClick
                    )
                    FloatingActionButton(
                        onClick = onCreateWidgetProfileClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.widget_profile_list_create_new))
                    }
                }
            }
        }
    }

}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileListScreen_Empty() {
    DevDrawerTheme {
        WidgetProfileListScreen(viewState = WidgetProfilesViewModel.ViewState.Loaded(emptyList()))
    }
}
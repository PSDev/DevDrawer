package de.psdev.devdrawer.profiles.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.DeleteDialogState
import de.psdev.devdrawer.profiles.WidgetInUseErrorAlertDialog
import de.psdev.devdrawer.profiles.WidgetProfileList
import de.psdev.devdrawer.profiles.WidgetProfilesViewModel
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import java.util.UUID

@Composable
fun WidgetProfilesScreen(
    viewModel: WidgetProfilesViewModel = hiltViewModel(),
    onEditProfile: (WidgetProfile) -> Unit
) {
    var deleteDialogShown by remember { mutableStateOf<DeleteDialogState>(DeleteDialogState.Hidden) }
    val viewState by viewModel.viewState.collectAsState()

    val onDeleteProfile: (WidgetProfile) -> Unit = { widgetProfile ->
        viewModel.prepareProfileDeletion(widgetProfile) { state ->
            deleteDialogShown = state
        }
    }

    WidgetProfileListScreen(
        viewState = viewState,
        onWidgetProfileClick = onEditProfile,
        onWidgetProfileLongClick = onDeleteProfile,
        onEditProfile = onEditProfile,
        onDeleteProfile = onDeleteProfile,
        onCreateWidgetProfileClick = {
            viewModel.createNewProfile { widgetProfile ->
                onEditProfile(widgetProfile)
            }
        }
    )
    when (val state = deleteDialogShown) {
        DeleteDialogState.Hidden -> Unit
        is DeleteDialogState.Showing -> AlertDialog(
            onDismissRequest = { deleteDialogShown = DeleteDialogState.Hidden },
            title = {
                Text(text = "Confirm")
            },
            text = {
                Text(text = "Do you really want to delete the profile '${state.widgetProfile.name}'?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProfile(state.widgetProfile)
                    deleteDialogShown = DeleteDialogState.Hidden
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
    viewState: UiState<List<WidgetProfile>>,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onWidgetProfileLongClick: (WidgetProfile) -> Unit = {},
    onEditProfile: (WidgetProfile) -> Unit = {},
    onDeleteProfile: (WidgetProfile) -> Unit = {},
    onCreateWidgetProfileClick: () -> Unit = {}
) {
    when (viewState) {
        is UiState.Loading, is UiState.Error -> LoadingView()
        is UiState.Success -> {
            val profiles = viewState.data
            if (profiles.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
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
                        onWidgetProfileLongClick = onWidgetProfileLongClick,
                        onEditProfile = onEditProfile,
                        onDeleteProfile = onDeleteProfile
                    )
                    FloatingActionButton(
                        onClick = onCreateWidgetProfileClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(id = R.string.widget_profile_list_create_new)
                        )
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
        WidgetProfileListScreen(
            viewState = UiState.Success(emptyList())
        )
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileListScreen_Profiles() {
    DevDrawerTheme {
        WidgetProfileListScreen(
            viewState = UiState.Success(
                listOf(
                    WidgetProfile(UUID.randomUUID().toString(), "Profile 1"),
                    WidgetProfile(UUID.randomUUID().toString(), "Profile 2"),
                )
            )
        )
    }
}

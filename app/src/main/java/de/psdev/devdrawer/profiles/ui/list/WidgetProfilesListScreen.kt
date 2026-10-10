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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.DeleteDialogState
import de.psdev.devdrawer.profiles.DeleteResult
import de.psdev.devdrawer.profiles.ProfileSummary
import de.psdev.devdrawer.profiles.WidgetInUseErrorAlertDialog
import de.psdev.devdrawer.profiles.WidgetProfileList
import de.psdev.devdrawer.profiles.WidgetProfilesViewModel
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.launch

@Composable
fun WidgetProfilesScreen(
    viewModel: WidgetProfilesViewModel = hiltViewModel(),
    onEditProfile: (WidgetProfile) -> Unit,
    onProfileCreated: (WidgetProfile) -> Unit = onEditProfile
) {
    var deleteDialogShown by remember { mutableStateOf<DeleteDialogState>(DeleteDialogState.Hidden) }
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    Box(modifier = Modifier.fillMaxSize()) {
        WidgetProfileListScreen(
            viewState = viewState,
            onWidgetProfileClick = onEditProfile,
            onDuplicateProfile = viewModel::duplicateProfile,
            onDeleteProfile = { profile ->
                viewModel.deleteProfile(profile) { result ->
                    when (result) {
                        is DeleteResult.InUse -> deleteDialogShown = DeleteDialogState.InUseError(result.profile, result.widgets)
                        is DeleteResult.Deleted -> scope.launch {
                            val action = snackbarHostState.showSnackbar(
                                message = resources.getString(R.string.profile_deleted, result.profile.name),
                                actionLabel = resources.getString(R.string.undo),
                                duration = SnackbarDuration.Long
                            )
                            if (action == SnackbarResult.ActionPerformed) viewModel.undoDelete(result)
                        }
                    }
                }
            },
            onCreateWidgetProfileClick = {
                viewModel.createNewProfile(onProfileCreated)
            }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    when (val state = deleteDialogShown) {
        DeleteDialogState.Hidden -> Unit
        is DeleteDialogState.InUseError -> {
            WidgetInUseErrorAlertDialog(state, onDismiss = {
                deleteDialogShown = DeleteDialogState.Hidden
            })
        }
    }
}

@Composable
fun WidgetProfileListScreen(
    viewState: UiState<List<ProfileSummary>>,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onDuplicateProfile: (WidgetProfile) -> Unit = {},
    onDeleteProfile: (WidgetProfile) -> Unit = {},
    onCreateWidgetProfileClick: () -> Unit = {}
) {
    when (viewState) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(text = stringResource(R.string.profiles_load_failed), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.error_message, viewState.cause.message ?: viewState.cause.javaClass.simpleName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                            contentDescription = null
                        )
                        Text(text = stringResource(id = R.string.widget_profile_list_create_new))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    WidgetProfileList(
                        profiles = profiles,
                        onWidgetProfileClick = onWidgetProfileClick,
                        onDuplicateProfile = onDuplicateProfile,
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
        WidgetProfileListScreen(viewState = UiState.Success(emptyList()))
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
                    ProfileSummary(WidgetProfile(name = "Signed like DevDrawer2"), 1, 7, listOf("Work apps")),
                    ProfileSummary(WidgetProfile(name = "Default"), 0, 0, emptyList())
                )
            )
        )
    }
}

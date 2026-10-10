package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.AddAppSignaturePackageFilterDialogViewModel.ViewState
import de.psdev.devdrawer.ui.loading.LoadingView

/** Picks an installed app whose signing key becomes a filter: every app signed with the same key then matches. */
@Composable
fun AddAppSignatureFilterSheet(
    currentFilters: List<PackageFilter>,
    viewModel: AddAppSignaturePackageFilterDialogViewModel = hiltViewModel(),
    closeDialog: () -> Unit = {},
    appSelected: (AppInfo) -> Unit = {}
) {
    val viewState by remember(viewModel) { viewModel.availableApps(currentFilters) }
        .collectAsState(initial = ViewState.Loading)
    AddAppSignatureFilterSheet(
        viewState = viewState,
        closeDialog = closeDialog,
        appSelected = appSelected,
        showSystemApps = { viewModel.showSystemApps.value = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAppSignatureFilterSheet(
    viewState: ViewState,
    closeDialog: () -> Unit = {},
    appSelected: (AppInfo) -> Unit = {},
    showSystemApps: (Boolean) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = closeDialog) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.select_signature_from_app), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.signature_sheet_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii)
            )
            val includeSystemApps = (viewState as? ViewState.Loaded)?.showSystemApps == true
            FilterChip(
                selected = includeSystemApps,
                enabled = viewState is ViewState.Loaded,
                onClick = { showSystemApps(!includeSystemApps) },
                label = { Text(stringResource(R.string.include_system_apps)) },
                leadingIcon = if (includeSystemApps) {
                    { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                }
            )
        }
        when (viewState) {
            ViewState.Loading -> LoadingView(modifier = Modifier.fillMaxWidth().padding(24.dp), showText = false)
            is ViewState.Error -> Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.error_message, viewState.message)
            )
            is ViewState.Loaded -> {
                val apps = viewState.data.filter { app ->
                    query.isBlank() || app.name.contains(query.trim(), ignoreCase = true) ||
                        app.packageName.contains(query.trim(), ignoreCase = true)
                }
                if (apps.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(
                            if (viewState.data.isEmpty()) R.string.no_apps_available else R.string.search_no_results
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        items(apps, key = { it.packageName }) { app ->
                            AppInfoItem(appInfo = app, onAppClicked = appSelected)
                        }
                    }
                }
            }
        }
    }
}

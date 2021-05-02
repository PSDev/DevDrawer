package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.AddAppSignaturePackageFilterDialogViewModel.ViewState
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun AddAppSignaturePackageFilterDialog(
    currentFilters: List<PackageFilter>,
    viewModel: AddAppSignaturePackageFilterDialogViewModel = hiltViewModel(),
    closeDialog: () -> Unit = {},
    appSelected: (AppInfo) -> Unit = {}
) {
    val viewState by remember(viewModel) { viewModel.availableApps(currentFilters) }
        .collectAsState(initial = ViewState.Loading)
    AddAppSignaturePackageFilterDialog(
        viewState = viewState,
        closeDialog = closeDialog,
        appSelected = appSelected
    )
}

@Composable
private fun AddAppSignaturePackageFilterDialog(
    viewState: ViewState,
    closeDialog: () -> Unit = {},
    appSelected: (AppInfo) -> Unit = {}
) {
    Dialog(
        onDismissRequest = closeDialog,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(modifier = Modifier.padding(16.dp), color = MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_certificate), contentDescription = stringResource(id = R.string.app_signature))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.enter_package_name_filter))
                }
                Spacer(modifier = Modifier.size(4.dp))
                Divider()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    when (viewState) {
                        ViewState.Loading -> LoadingView(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp), showText = false
                        )
                        is ViewState.Loaded -> {
                            Column {
                                if (viewState.data.isEmpty()) {
                                    Text(text = stringResource(id = R.string.no_apps_available))
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                    ) {
                                        items(viewState.data) {
                                            AppInfoItem(appInfo = it, onAppClicked = appSelected)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                TextButton(modifier = Modifier.align(Alignment.End), onClick = closeDialog) {
                                    Text(text = stringResource(id = R.string.cancel).toUpperCase(Locale.current))
                                }
                            }
                        }
                        is ViewState.Error -> Text(text = "Error: ${viewState.message}")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_AddAppSignaturePackageFilterDialog() {
    DevDrawerTheme {
        Surface {
            AddAppSignaturePackageFilterDialog(
                viewState = ViewState.Loaded(
                    emptyList()
                )
            )
        }
    }
}
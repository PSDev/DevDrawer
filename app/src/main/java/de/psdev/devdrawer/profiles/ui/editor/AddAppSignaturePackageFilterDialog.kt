package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.AddAppSignaturePackageFilterDialogViewModel.ViewState
import de.psdev.devdrawer.ui.dialog.DefaultDialog
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
        appSelected = appSelected,
        showSystemApps = { viewModel.showSystemApps.value = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAppSignaturePackageFilterDialog(
    viewState: ViewState,
    closeDialog: () -> Unit = {},
    appSelected: (AppInfo) -> Unit = {},
    showSystemApps: (Boolean) -> Unit = {}
) {
    DefaultDialog(
        onDismissRequest = closeDialog,
        titleContent = {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_certificate),
                    contentDescription = stringResource(id = R.string.app_signature)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.select_signature_from_app))
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(text = stringResource(id = R.string.toggle_system_apps))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        enabled = viewState is ViewState.Loaded,
                        onClick = {
                            if (viewState is ViewState.Loaded) {
                                showSystemApps(!viewState.showSystemApps)
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Filled.SettingsApplications,
                            contentDescription = stringResource(id = R.string.include_system_apps)
                        )
                    }
                }
            }
        },
        bottomContent = {
            TextButton(
                modifier = Modifier
                    .align(Alignment.End), onClick = closeDialog
            ) {
                Text(text = stringResource(id = R.string.cancel).toUpperCase(Locale.current))
            }
        }
    ) {
        when (viewState) {
            ViewState.Loading -> LoadingView(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .wrapContentHeight(),
                showText = false
            )

            is ViewState.Loaded -> {
                if (viewState.data.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.no_apps_available)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, false)
                    ) {
                        items(viewState.data) {
                            AppInfoItem(appInfo = it, onAppClicked = appSelected)
                        }
                    }
                }
            }

            is ViewState.Error -> Text(text = "Error: ${viewState.message}")
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
                    emptyList(),
                    false
                )
            )
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_AddAppSignaturePackageFilterDialog_Apps() {
    DevDrawerTheme {
        Surface {
            val resources = LocalResources.current
            val bitmap = createBitmap(10, 10)
            AddAppSignaturePackageFilterDialog(
                viewState = ViewState.Loaded(
                    (1..12).map { i ->
                        AppInfo(
                            name = "App $i",
                            packageName = "com.example.app$i",
                            bitmap.toDrawable(resources),
                            0,
                            0,
                            ""
                        )
                    },
                    false
                )
            )
        }
    }
}
package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialogViewModel.ViewState.Error
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialogViewModel.ViewState.Loaded
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialogViewModel.ViewState.Loading
import de.psdev.devdrawer.ui.dialog.DefaultDialog
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun PackageFilterPreviewDialog(
    packageFilter: PackageFilter,
    viewModel: PackageFilterPreviewDialogViewModel = hiltViewModel(),
    closeDialog: () -> Unit = {}
) {
    val viewState by remember(viewModel) { viewModel.load(packageFilter) }.collectAsState(initial = Loading)
    PackageFilterPreviewDialog(
        viewState = viewState, closeDialog = closeDialog
    )
}

@Composable
private fun PackageFilterPreviewDialog(
    viewState: PackageFilterPreviewDialogViewModel.ViewState,
    closeDialog: () -> Unit = {}
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
                Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.apps_matching_filter))
            }
        },
        bottomContent = {
            TextButton(modifier = Modifier.align(Alignment.End), onClick = closeDialog) {
                Text(text = stringResource(id = R.string.close).toUpperCase(Locale.current))
            }
        }
    ) {
        when (viewState) {
            Loading -> LoadingView(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                showText = false
            )

            is Loaded -> LazyColumn(modifier = Modifier.weight(1f, false)) {
                items(viewState.data) { appInfo ->
                    AppInfoItem(appInfo = appInfo)
                }
            }

            is Error -> Text(text = "Error: ${viewState.message}")
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_PackageFilterPreviewDialog() {
    val context = LocalContext.current
    val resources = context.resources
    DevDrawerTheme {
        Surface {
            val baseAppInfo = AppInfo(
                name = "Test  app",
                packageName = "Test package",
                appIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_foreground, context.theme)!!,
                firstInstallTime = System.currentTimeMillis(),
                lastUpdateTime = System.currentTimeMillis(),
                signatureHashSha256 = "1234"
            )
            PackageFilterPreviewDialog(
                viewState = Loaded(
                    listOf(
                        baseAppInfo,
                        baseAppInfo.copy(name = "App 2"),
                    )
                )
            )
        }
    }
}
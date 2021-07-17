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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialogViewModel.ViewState.*
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import java.util.*

@Composable
fun PackageFilterPreviewDialog(
    packageFilter: PackageFilter,
    viewModel: PackageFilterPreviewDialogViewModel = hiltViewModel(),
    closeDialog: () -> Unit = {}
) {
    val viewState by remember(viewModel) { viewModel.load(packageFilter) }.collectAsState(initial = Loading)
    PackageFilterPreviewDialog(
        viewState = viewState,
        closeDialog = closeDialog
    )
}

@Composable
private fun PackageFilterPreviewDialog(
    viewState: PackageFilterPreviewDialogViewModel.ViewState,
    closeDialog: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { closeDialog() }
    ) {
        Surface(modifier = Modifier.padding(16.dp), color = MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_certificate), contentDescription = stringResource(id = R.string.app_signature))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.apps_matching_filter))
                }
                Spacer(modifier = Modifier.size(4.dp))
                Divider()
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    when (viewState) {
                        Loading -> LoadingView(modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp), showText = false)
                        is Loaded -> LazyColumn {
                            items(viewState.data) { appInfo ->
                                AppInfoItem(appInfo = appInfo)
                            }
                        }
                        is Error -> Text(text = "Error: ${viewState.message}")
                    }
                }
                TextButton(modifier = Modifier.align(Alignment.End), onClick = closeDialog) {
                    Text(text = stringResource(id = R.string.close).uppercase(Locale.getDefault()))
                }
            }
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
                        baseAppInfo.copy(name = "App 3"),
                        baseAppInfo.copy(name = "App 4"),
                        baseAppInfo.copy(name = "App 5"),
                        baseAppInfo.copy(name = "App 6"),
                        baseAppInfo.copy(name = "App 7"),
                    )
                )
            )
        }
    }
}
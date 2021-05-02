package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialogViewModel.ViewState.*
import de.psdev.devdrawer.ui.autocomplete.AutoCompleteTextView
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import java.util.*

@Composable
fun AddPackageNamePackageFilterDialog(
    currentFilters: List<PackageFilter>,
    viewModel: AddPackageNamePackageFilterDialogViewModel = hiltViewModel(),
    closeDialog: () -> Unit = {},
    addFilter: (String) -> Unit = {}
) {
    val viewState by remember(viewModel) { viewModel.availablePackageFilters(currentFilters) }
        .collectAsState(initial = Loading)
    AddPackageNamePackageFilterDialog(
        viewState = viewState,
        closeDialog = closeDialog,
        addFilter = addFilter
    )
}

@Composable
private fun AddPackageNamePackageFilterDialog(
    viewState: AddPackageNamePackageFilterDialogViewModel.ViewState,
    closeDialog: () -> Unit = {},
    addFilter: (String) -> Unit = {}
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
                        Loading -> LoadingView(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp), showText = false
                        )
                        is Loaded -> {
                            Column {
                                var text by remember { mutableStateOf("") }
                                AutoCompleteTextView(
                                    options = viewState.data,
                                    label = { Text(text = stringResource(id = R.string.packagefilter)) },
                                    onTextChanged = { text = it }
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = closeDialog) {
                                        Text(text = stringResource(id = R.string.cancel).uppercase(Locale.getDefault()))
                                    }
                                    TextButton(onClick = { addFilter(text) }, enabled = text.isNotBlank()) {
                                        Text(text = stringResource(id = R.string.add).uppercase(Locale.getDefault()))
                                    }
                                }
                            }
                        }
                        is Error -> Text(text = "Error: ${viewState.message}")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_AddPackageNamePackageFilterDialog() {
    DevDrawerTheme {
        Surface {
            AddPackageNamePackageFilterDialog(
                viewState = Loaded(
                    listOf(
                        "com.example.1",
                        "com.example.2",
                        "com.example.3",
                        "com.example.4",
                        "com.example.5",
                        "com.example.6",
                    )
                )
            )
        }
    }
}
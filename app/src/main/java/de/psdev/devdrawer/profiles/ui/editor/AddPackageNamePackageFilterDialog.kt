package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialogViewModel.ViewState.Error
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialogViewModel.ViewState.Loaded
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialogViewModel.ViewState.Loading
import de.psdev.devdrawer.ui.autocomplete.AutoCompleteTextView
import de.psdev.devdrawer.ui.loading.LoadingView
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

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
        Surface(modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_certificate), contentDescription = stringResource(id = R.string.app_signature))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.enter_package_name_filter))
                }
                Spacer(modifier = Modifier.size(4.dp))
                HorizontalDivider()
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
                                        Text(text = stringResource(id = R.string.cancel).toUpperCase(Locale.current))
                                    }
                                    TextButton(onClick = { addFilter(text) }, enabled = text.isNotBlank()) {
                                        Text(text = stringResource(id = R.string.add).toUpperCase(Locale.current))
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
                    (1..6).map { "com.example.$it" }
                )
            )
        }
    }
}
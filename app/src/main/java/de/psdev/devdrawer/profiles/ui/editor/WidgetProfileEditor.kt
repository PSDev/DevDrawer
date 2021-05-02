package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WidgetProfileEditor(
    viewModel: WidgetProfileEditorViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState(initial = WidgetProfileEditorViewState.Empty)
    var currentDialog by remember { mutableStateOf<WidgetProfileEditorDialogs>(WidgetProfileEditorDialogs.None) }
    val coroutineScope = rememberCoroutineScope()


    Box(modifier = Modifier.fillMaxSize()) {
        WidgetProfileEditor(
            viewState = viewState,
            onNameChange = {
                viewModel.onNameChanged(it)
            },
            onSaveNameClick = {
                viewModel.saveChanges(viewState)
            },
            onAddPackageFilterClick = {
                currentDialog = WidgetProfileEditorDialogs.AddPackageNamePackageFilter(viewState.packageFilters)
            },
            onAddAppSignatureClick = {
                currentDialog = WidgetProfileEditorDialogs.AddAppSignaturePackageFilter(viewState.packageFilters)
            },
            onPackageFilterPreviewClick = {
                currentDialog = WidgetProfileEditorDialogs.PackageFilterPreview(it)
            },
            onPackageFilterInfoClick = { },
            onDeletePackageFilterClick = {
                currentDialog = WidgetProfileEditorDialogs.DeletePackageFilter(it)
            }
        )
    }
    when (val dialog = currentDialog) {
        WidgetProfileEditorDialogs.None -> Unit
        is WidgetProfileEditorDialogs.AddAppSignaturePackageFilter -> AddAppSignaturePackageFilterDialog(
            currentFilters = dialog.currentPackageFilters,
            closeDialog = {
                currentDialog = WidgetProfileEditorDialogs.None
            },
            appSelected = { appInfo ->
                coroutineScope.launch {
                    viewModel.addPackageFilter(
                        PackageFilter(
                            filter = appInfo.signatureHashSha256,
                            type = FilterType.SIGNATURE,
                            description = appInfo.name,
                            profileId = viewState.widgetProfile?.id.orEmpty()
                        )
                    )
                    currentDialog = WidgetProfileEditorDialogs.None
                }
            }
        )
        is WidgetProfileEditorDialogs.AddPackageNamePackageFilter -> AddPackageNamePackageFilterDialog(
            currentFilters = dialog.currentPackageFilters,
            closeDialog = {
                currentDialog = WidgetProfileEditorDialogs.None
            },
            addFilter = { packageNameFilter ->
                coroutineScope.launch {
                    viewModel.addPackageFilter(
                        PackageFilter(
                            type = FilterType.PACKAGE_NAME,
                            filter = packageNameFilter,
                            profileId = viewState.widgetProfile?.id.orEmpty()
                        )
                    )
                    currentDialog = WidgetProfileEditorDialogs.None
                }
            }
        )
        is WidgetProfileEditorDialogs.PackageFilterPreview -> PackageFilterPreviewDialog(packageFilter = dialog.packageFilter) {
            currentDialog = WidgetProfileEditorDialogs.None
        }
        is WidgetProfileEditorDialogs.PackageFilterInfo -> Unit
        is WidgetProfileEditorDialogs.DeletePackageFilter -> {
            DeletePackageFilterDialog(
                onDismiss = {
                    currentDialog = WidgetProfileEditorDialogs.None
                },
                onConfirm = {
                    coroutineScope.launch {
                        viewModel.deleteFilter(dialog.packageFilter)
                        currentDialog = WidgetProfileEditorDialogs.None
                    }
                }
            )
        }
    }
}

private sealed class WidgetProfileEditorDialogs {
    object None: WidgetProfileEditorDialogs()

    data class PackageFilterPreview(
        val packageFilter: PackageFilter
    ): WidgetProfileEditorDialogs()

    data class AddPackageNamePackageFilter(
        val currentPackageFilters: List<PackageFilter>
    ): WidgetProfileEditorDialogs()

    data class AddAppSignaturePackageFilter(
        val currentPackageFilters: List<PackageFilter>
    ): WidgetProfileEditorDialogs()

    data class PackageFilterInfo(
        val packageFilter: PackageFilter
    ): WidgetProfileEditorDialogs()

    data class DeletePackageFilter(
        val packageFilter: PackageFilter
    ): WidgetProfileEditorDialogs()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun WidgetProfileEditor(
    viewState: WidgetProfileEditorViewState,
    onNameChange: (String) -> Unit = {},
    onSaveNameClick: () -> Unit = {},
    onAddPackageFilterClick: (WidgetProfile) -> Unit = {},
    onAddAppSignatureClick: (WidgetProfile) -> Unit = {},
    onPackageFilterPreviewClick: (PackageFilter) -> Unit = {},
    onPackageFilterInfoClick: (PackageFilter) -> Unit = {},
    onDeletePackageFilterClick: (PackageFilter) -> Unit = {}
) {
    val widgetProfile = viewState.widgetProfile
    if (widgetProfile == null) {
        // Loading
        Box(modifier = Modifier.defaultMinSize(minHeight = 256.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
    } else {
        Column(modifier = Modifier.defaultMinSize(minHeight = 256.dp)) {
            Surface(modifier = Modifier.wrapContentHeight(), elevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(8.dp)
                ) {
                    WidgetProfileName(
                        widgetName = viewState.widgetName ?: widgetProfile.name,
                        currentName = widgetProfile.name,
                        onNameChange = onNameChange,
                        onSaveNameClick = onSaveNameClick
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onAddPackageFilterClick(widgetProfile) }
                        ) {
                            Icon(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                painter = painterResource(id = R.drawable.ic_regex),
                                contentDescription = stringResource(id = R.string.add_package_name)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.add_package_name).uppercase(Locale.getDefault()))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onAddAppSignatureClick(widgetProfile) }
                        ) {
                            Icon(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                painter = painterResource(id = R.drawable.ic_certificate),
                                contentDescription = stringResource(id = R.string.add_app_signature)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.add_app_signature).uppercase(Locale.getDefault()))
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewState.packageFilters) { packageFilter ->
                    Card {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconRes = when (packageFilter.type) {
                                FilterType.PACKAGE_NAME -> R.drawable.ic_regex
                                FilterType.SIGNATURE -> R.drawable.ic_certificate
                            }
                            Icon(
                                modifier = Modifier.padding(8.dp),
                                painter = painterResource(id = iconRes),
                                contentDescription = null
                            )
                            val text = when (packageFilter.type) {
                                FilterType.PACKAGE_NAME -> packageFilter.filter
                                FilterType.SIGNATURE -> packageFilter.description
                            }
                            Text(modifier = Modifier.weight(1f), text = text)
                            AnimatedVisibility(visible = packageFilter.type == FilterType.SIGNATURE) {
                                Icon(
                                    modifier = Modifier
                                        .clickable { onPackageFilterInfoClick(packageFilter) }
                                        .padding(8.dp),
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null
                                )
                            }
                            Icon(
                                modifier = Modifier
                                    .clickable { onPackageFilterPreviewClick(packageFilter) }
                                    .padding(8.dp),
                                imageVector = Icons.Filled.Preview,
                                contentDescription = null
                            )
                            Icon(
                                modifier = Modifier
                                    .clickable { onDeletePackageFilterClick(packageFilter) }
                                    .padding(8.dp),
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WidgetProfileName(
    widgetName: String,
    currentName: String,
    onNameChange: (String) -> Unit = {},
    onSaveNameClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            singleLine = true,
            value = widgetName,
            onValueChange = onNameChange,
            label = { Text(text = stringResource(id = R.string.name)) }
        )
        AnimatedVisibility(visible = widgetName != currentName) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSaveNameClick
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Outlined.Save,
                    contentDescription = stringResource(id = R.string.save)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileEditor_Loading() {
    DevDrawerTheme {
        WidgetProfileEditor(
            viewState = WidgetProfileEditorViewState.Empty
        )
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileEditor_Loaded() {
    val widgetProfile = WidgetProfile(
        id = UUID.randomUUID().toString(),
        name = "Test widget profile"
    )
    DevDrawerTheme {
        WidgetProfileEditor(
            viewState = WidgetProfileEditorViewState(
                widgetProfile = widgetProfile,
                widgetName = widgetProfile.name,
                packageFilters = listOf(
                    PackageFilter(profileId = widgetProfile.id, filter = "01022402020", type = FilterType.SIGNATURE),
                    PackageFilter(profileId = widgetProfile.id, filter = "com.example2.*")
                )
            )
        )
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileEditor_NameChanged() {
    DevDrawerTheme {
        WidgetProfileEditor(
            viewState = WidgetProfileEditorViewState(
                widgetProfile = WidgetProfile(
                    id = UUID.randomUUID().toString(),
                    name = "Test widget profile"
                ),
                widgetName = "Test widget profile 2"
            )
        )
    }
}
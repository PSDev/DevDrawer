package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlinx.coroutines.launch

private const val COLLAPSED_APP_COUNT = 5

@Composable
fun WidgetProfileEditor(
    profileId: String,
    isNew: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: WidgetProfileEditorViewModel = hiltViewModel(
        creationCallback = { factory: WidgetProfileEditorViewModel.Factory ->
            factory.create(profileId, isNew)
        }
    )
) {
    val viewState by viewModel.state.collectAsState(initial = WidgetProfileEditorViewState.Empty)
    var currentDialog by remember { mutableStateOf<WidgetProfileEditorDialogs>(WidgetProfileEditorDialogs.None) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // Changes save as they are made; a name still being typed is saved when the editor closes.
    DisposableEffect(viewModel) {
        onDispose { viewModel.onEditorClosed() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        WidgetProfileEditor(
            viewState = viewState,
            modifier = Modifier.fillMaxSize(),
            onNameChange = viewModel::onNameChanged,
            onAddFilterClick = { currentDialog = WidgetProfileEditorDialogs.AddFilter },
            onPackageFilterClick = { currentDialog = WidgetProfileEditorDialogs.PackageFilterInfo(it) },
            onRemoveFilterClick = { filter ->
                viewModel.deleteFilter(filter)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(R.string.filter_removed),
                        actionLabel = resources.getString(R.string.undo),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreFilter(filter)
                }
            }
        )
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    when (val dialog = currentDialog) {
        WidgetProfileEditorDialogs.None -> Unit
        WidgetProfileEditorDialogs.AddFilter -> AddFilterSheet(
            onDismiss = { currentDialog = WidgetProfileEditorDialogs.None },
            onSignatureClick = { currentDialog = WidgetProfileEditorDialogs.AddAppSignaturePackageFilter(viewState.packageFilters) },
            onPatternClick = {
                viewModel.onPatternChanged("")
                currentDialog = WidgetProfileEditorDialogs.AddPackagePattern
            }
        )
        is WidgetProfileEditorDialogs.AddAppSignaturePackageFilter -> AddAppSignatureFilterSheet(
            currentFilters = dialog.currentPackageFilters,
            closeDialog = {
                currentDialog = WidgetProfileEditorDialogs.None
            },
            appSelected = { appInfo ->
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
        )
        WidgetProfileEditorDialogs.AddPackagePattern -> {
            val preview by viewModel.patternPreview.collectAsState()
            PackagePatternSheet(
                preview = preview,
                onPatternChange = viewModel::onPatternChanged,
                onAdd = { pattern ->
                    viewModel.addPackageFilter(
                        PackageFilter(type = FilterType.PACKAGE_NAME, filter = pattern, profileId = viewState.widgetProfile?.id.orEmpty())
                    )
                    currentDialog = WidgetProfileEditorDialogs.None
                },
                onDismiss = { currentDialog = WidgetProfileEditorDialogs.None }
            )
        }
        is WidgetProfileEditorDialogs.PackageFilterInfo -> PackageFilterInfoDialog(
            packageFilter = dialog.packageFilter,
            onDismiss = {
                currentDialog = WidgetProfileEditorDialogs.None
            }
        )
    }
}

private sealed class WidgetProfileEditorDialogs {
    data object None : WidgetProfileEditorDialogs()
    data object AddFilter : WidgetProfileEditorDialogs()
    data object AddPackagePattern : WidgetProfileEditorDialogs()

    data class AddAppSignaturePackageFilter(
        val currentPackageFilters: List<PackageFilter>
    ) : WidgetProfileEditorDialogs()

    data class PackageFilterInfo(
        val packageFilter: PackageFilter
    ) : WidgetProfileEditorDialogs()
}

@Composable
internal fun WidgetProfileEditor(
    viewState: WidgetProfileEditorViewState,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onAddFilterClick: () -> Unit = {},
    onPackageFilterClick: (PackageFilter) -> Unit = {},
    onRemoveFilterClick: (PackageFilter) -> Unit = {}
) {
    val widgetProfile = viewState.widgetProfile
    if (widgetProfile == null) {
        // Loading
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
        return
    }
    var showAllApps by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = viewState.widgetName ?: widgetProfile.name,
            onValueChange = onNameChange,
            label = { Text(text = stringResource(id = R.string.name)) },
            // Every edit here changes these widgets, so say which they are.
            supportingText = {
                Text(
                    if (viewState.usedByWidgets.isEmpty()) {
                        stringResource(R.string.profile_not_used)
                    } else {
                        stringResource(R.string.profile_used_by, viewState.usedByWidgets.joinToString())
                    }
                )
            }
        )

        Column {
            SectionTitle(stringResource(R.string.filters_heading))
            if (viewState.packageFilters.isEmpty()) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(R.string.no_filters_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            viewState.packageFilters.forEachIndexed { index, packageFilter ->
                if (index > 0) HorizontalDivider()
                FilterRow(
                    packageFilter = packageFilter,
                    appCount = viewState.filterAppCounts[packageFilter.id] ?: 0,
                    onClick = { onPackageFilterClick(packageFilter) },
                    onRemove = { onRemoveFilterClick(packageFilter) }
                )
            }
            FilledTonalButton(modifier = Modifier.padding(top = 8.dp), onClick = onAddFilterClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.add_filter))
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                SectionTitle(stringResource(R.string.matching_apps, viewState.matchingApps.size))
                val apps = if (showAllApps) viewState.matchingApps else viewState.matchingApps.take(COLLAPSED_APP_COUNT)
                apps.forEach { app -> AppInfoItem(appInfo = app) }
                if (!showAllApps && viewState.matchingApps.size > COLLAPSED_APP_COUNT) {
                    TextButton(onClick = { showAllApps = true }) {
                        Text(stringResource(R.string.show_all, viewState.matchingApps.size))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

/** A filter in plain words: "Signed like DevDrawer2" or "Package matches com.example.*". */
@Composable
internal fun PackageFilter.label(): String = when (type) {
    FilterType.SIGNATURE -> stringResource(R.string.profile_name_signed_like, description.ifBlank { filter.take(SHORT_HASH_LENGTH) })
    FilterType.PACKAGE_NAME -> stringResource(R.string.filter_package_matches, filter)
}

private const val SHORT_HASH_LENGTH = 8

@Composable
private fun FilterRow(packageFilter: PackageFilter, appCount: Int, onClick: () -> Unit, onRemove: () -> Unit) {
    val label = packageFilter.label()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> R.drawable.ic_regex
                    FilterType.SIGNATURE -> R.drawable.ic_certificate
                }
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                text = stringResource(
                    R.string.profile_counts,
                    stringResource(
                        when (packageFilter.type) {
                            FilterType.PACKAGE_NAME -> R.string.setup_source_pattern
                            FilterType.SIGNATURE -> R.string.filter_type_signature
                        }
                    ),
                    pluralStringResource(R.plurals.widget_app_count, appCount, appCount)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRemove) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.remove_filter, label))
        }
    }
}

/** Both filter types side by side, each explained in one sentence, so the choice is by intent. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddFilterSheet(onDismiss: () -> Unit, onSignatureClick: () -> Unit, onPatternClick: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.add_filter), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.add_filter_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterTypeOption(
                iconRes = R.drawable.ic_certificate,
                title = stringResource(R.string.add_filter_signature_title),
                text = stringResource(R.string.add_filter_signature_text),
                onClick = onSignatureClick
            )
            FilterTypeOption(
                iconRes = R.drawable.ic_regex,
                title = stringResource(R.string.setup_source_pattern),
                text = stringResource(R.string.add_filter_pattern_text),
                onClick = onPatternClick
            )
        }
    }
}

@Composable
private fun FilterTypeOption(iconRes: Int, title: String, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileEditor_Loaded() {
    val widgetProfile = WidgetProfile(id = "p1", name = "Test widget profile")
    DevDrawerTheme {
        WidgetProfileEditor(
            viewState = WidgetProfileEditorViewState(
                widgetProfile = widgetProfile,
                widgetName = widgetProfile.name,
                packageFilters = listOf(
                    PackageFilter(id = "a", profileId = "p1", filter = "01022402020", type = FilterType.SIGNATURE, description = "DevDrawer2"),
                    PackageFilter(id = "b", profileId = "p1", filter = "com.example2.*")
                ),
                filterAppCounts = mapOf("a" to 2, "b" to 5)
            )
        )
    }
}

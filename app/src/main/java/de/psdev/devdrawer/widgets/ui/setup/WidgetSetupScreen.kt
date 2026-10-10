package de.psdev.devdrawer.widgets.ui.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.ui.loading.LoadingView

private const val COLLAPSED_APP_COUNT = 5

@Composable
fun WidgetSetupScreen(
    widgetId: Int,
    onDone: (Widget) -> Unit,
    viewModel: WidgetSetupViewModel = hiltViewModel<WidgetSetupViewModel, WidgetSetupViewModel.Factory> {
        it.create(widgetId)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WidgetSetup(
        state = state,
        onSourceSelected = viewModel::onSourceSelected,
        onAppSelected = viewModel::onAppSelected,
        onPatternChanged = viewModel::onPatternChanged,
        onProfileSelected = viewModel::onProfileSelected,
        onDoneClick = { viewModel.finish(onDone) }
    )
}

@Composable
fun WidgetSetup(
    state: WidgetSetupState,
    modifier: Modifier = Modifier,
    onSourceSelected: (SetupSource) -> Unit = {},
    onAppSelected: (String) -> Unit = {},
    onPatternChanged: (String) -> Unit = {},
    onProfileSelected: (String) -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    if (state.isLoading) {
        LoadingView(modifier = modifier.fillMaxSize())
        return
    }
    var showAllApps by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = stringResource(R.string.setup_question), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = stringResource(R.string.setup_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                SourceOption(
                    title = stringResource(R.string.setup_source_my_apps),
                    summary = stringResource(R.string.setup_source_my_apps_summary),
                    selected = state.source == SetupSource.MY_APPS,
                    onClick = { onSourceSelected(SetupSource.MY_APPS) }
                )
            }
            if (state.source == SetupSource.MY_APPS) {
                val visibleApps = if (showAllApps) state.apps else state.apps.take(COLLAPSED_APP_COUNT)
                items(visibleApps, key = { it.packageName }) { app ->
                    AppOption(app = app, selected = app.packageName == state.selectedPackageName, onClick = { onAppSelected(app.packageName) })
                }
                if (!showAllApps && state.apps.size > COLLAPSED_APP_COUNT) {
                    item {
                        TextButton(onClick = { showAllApps = true }) {
                            Text(stringResource(R.string.show_all, state.apps.size))
                        }
                    }
                }
            }
            item {
                SourceOption(
                    title = stringResource(R.string.setup_source_pattern),
                    summary = stringResource(R.string.setup_source_pattern_summary),
                    selected = state.source == SetupSource.PATTERN,
                    onClick = { onSourceSelected(SetupSource.PATTERN) }
                )
            }
            if (state.source == SetupSource.PATTERN) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.pattern,
                        onValueChange = onPatternChanged,
                        singleLine = true,
                        label = { Text(stringResource(R.string.packagefilter)) },
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii)
                    )
                }
            }
            item {
                SourceOption(
                    title = stringResource(R.string.setup_source_profile),
                    summary = stringResource(R.string.setup_source_profile_summary),
                    selected = state.source == SetupSource.PROFILE,
                    onClick = { onSourceSelected(SetupSource.PROFILE) }
                )
            }
            if (state.source == SetupSource.PROFILE) {
                if (state.profiles.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = stringResource(R.string.setup_no_profiles),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(state.profiles, key = { it.profile.id }) { option ->
                    ChoiceRow(
                        selected = option.profile.id == state.selectedProfileId,
                        onClick = { onProfileSelected(option.profile.id) },
                        title = option.profile.name,
                        subtitle = pluralStringResource(R.plurals.widget_app_count, option.appCount, option.appCount)
                    )
                }
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = pluralStringResource(R.plurals.setup_match_count, state.matchCount, state.matchCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(enabled = state.canFinish, onClick = onDoneClick) {
                Text(stringResource(R.string.setup_done))
            }
        }
    }
}

@Composable
private fun SourceOption(title: String, summary: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppOption(app: SetupApp, selected: Boolean, onClick: () -> Unit) {
    val subtitle = if (app.otherAppsWithSameKey > 0) {
        pluralStringResource(R.plurals.setup_also_matches, app.otherAppsWithSameKey, app.otherAppsWithSameKey)
    } else {
        app.packageName
    }
    ChoiceRow(selected = selected, onClick = onClick, title = app.name, subtitle = subtitle) {
        val icon = remember(app.icon) { app.icon?.toBitmap()?.asImageBitmap() }
        if (icon != null) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(40.dp))
        } else {
            Spacer(Modifier.size(40.dp))
        }
    }
}

@Composable
private fun ChoiceRow(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

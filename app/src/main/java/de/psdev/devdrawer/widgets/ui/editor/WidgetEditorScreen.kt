package de.psdev.devdrawer.widgets.ui.editor

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.psdev.devdrawer.AppBarActionsProvider
import de.psdev.devdrawer.ProvideMenu
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.ProfileWithAppCount
import de.psdev.devdrawer.settings.ListPreference
import de.psdev.devdrawer.settings.label
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.widgets.ui.widgetHeaderColors

@Composable
fun WidgetEditorScreen(
    id: Int,
    viewModel: WidgetEditorViewModel = hiltViewModel<WidgetEditorViewModel, WidgetEditorViewModel.Factory> {
        it.create(id)
    },
    menuCallback: AppBarActionsProvider,
    onBack: () -> Unit,
    onEditWidgetProfile: (WidgetProfile) -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val persistedWidget = viewState.persistedWidget
    ProvideMenu(menuCallback, persistedWidget) {
        if (persistedWidget != null) {
            IconButton(onClick = {
                viewModel.deleteWidget(persistedWidget)
                onBack()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_widget)
                )
            }
        }
    }
    WidgetEditor(
        viewState = viewState,
        onNameChange = viewModel::onNameChanged,
        onHeaderColorSelected = viewModel::onHeaderColorSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onEditWidgetProfile = onEditWidgetProfile,
        onWidgetProfileSelected = viewModel::onWidgetProfileSelected,
        onSaveChangesClick = viewModel::saveChanges
    )
}

@Composable
fun WidgetEditor(
    modifier: Modifier = Modifier,
    viewState: WidgetEditorViewState,
    onNameChange: (String) -> Unit = {},
    onHeaderColorSelected: (WidgetHeaderColor) -> Unit = {},
    onSortOrderSelected: (SortOrder?) -> Unit = {},
    onEditWidgetProfile: (WidgetProfile) -> Unit = {},
    onWidgetProfileSelected: (WidgetProfile) -> Unit = {},
    onSaveChangesClick: () -> Unit = {}
) {
    val widget = viewState.editableWidget
    if (widget == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WidgetPreview(
                widget = widget,
                apps = viewState.previewApps,
                appCount = viewState.previewAppCount
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                value = widget.name,
                onValueChange = onNameChange,
                label = { Text(text = stringResource(id = R.string.name)) }
            )
            HeaderColorPicker(selected = widget.headerColor, onSelected = onHeaderColorSelected)
            SectionTitle(stringResource(R.string.editor_apps))
            Column(modifier = Modifier.selectableGroup()) {
                viewState.profiles.forEach { option ->
                    ProfileRow(
                        option = option,
                        selected = option.profile.id == widget.profileId,
                        onSelected = { onWidgetProfileSelected(option.profile) },
                        onEdit = { onEditWidgetProfile(option.profile) }
                    )
                }
            }
            SortOrderPreference(
                current = widget.sortOrder,
                defaultSortOrder = viewState.defaultSortOrder,
                onSelected = onSortOrderSelected
            )
        }
        AnimatedVisibility(
            visible = viewState.isDirty,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FloatingActionButton(
                onClick = onSaveChangesClick,
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

/** The widget as the home screen will show it: header, then the first apps it lists. */
@Composable
private fun WidgetPreview(widget: Widget, apps: List<AppInfo>, appCount: Int) {
    val header = widgetHeaderColors(widget.headerColor)
    val previewDescription = stringResource(R.string.widget_preview)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
            .semantics { contentDescription = previewDescription }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(header.container, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = widget.name,
                        color = header.content,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (appCount == 0) {
                            stringResource(R.string.widget_no_apps)
                        } else {
                            pluralStringResource(R.plurals.widget_app_count, appCount, appCount)
                        },
                        color = header.content,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = header.content)
            }
            if (apps.isEmpty()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.widget_empty_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            apps.forEach { app ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = remember(app.packageName) { app.appIcon.toBitmap().asImageBitmap() }
                    Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = app.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderColorPicker(selected: WidgetHeaderColor, onSelected: (WidgetHeaderColor) -> Unit) {
    val options = WidgetHeaderColor.entries.filter {
        it != WidgetHeaderColor.DYNAMIC || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    val labels = mapOf(
        WidgetHeaderColor.AMBER to stringResource(R.string.header_color_amber),
        WidgetHeaderColor.NEUTRAL to stringResource(R.string.header_color_neutral),
        WidgetHeaderColor.DARK to stringResource(R.string.header_color_dark),
        WidgetHeaderColor.DYNAMIC to stringResource(R.string.header_color_dynamic)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.header_color),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        options.forEach { option ->
            val colors = widgetHeaderColors(option)
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .selectable(selected = isSelected, onClick = { onSelected(option) }, role = Role.RadioButton)
                    .semantics { contentDescription = labels.getValue(option) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.container, MaterialTheme.shapes.small)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (option == WidgetHeaderColor.DYNAMIC) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = colors.content)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(option: ProfileWithAppCount, selected: Boolean, onSelected: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelected, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(text = option.profile.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = pluralStringResource(R.plurals.widget_app_count, option.appCount, option.appCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_profile_named, option.profile.name)
            )
        }
    }
}

@Composable
private fun SortOrderPreference(current: SortOrder?, defaultSortOrder: SortOrder, onSelected: (SortOrder?) -> Unit) {
    val labels = stringArrayResource(R.array.sort_order_labels)
    val labelFor = { sortOrder: SortOrder -> sortOrder.label(labels) }
    val values: Map<SortOrder?, String> =
        mapOf<SortOrder?, String>(null to stringResource(R.string.sort_default, labelFor(defaultSortOrder))) +
            SortOrder.entries.associateWith { labelFor(it) }
    ListPreference(
        label = stringResource(R.string.sort_by),
        values = values,
        currentValue = current,
        onClick = onSelected
    )
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetEditor_Loaded() {
    val widgetProfile = WidgetProfile(id = "p1", name = "Signed like DevDrawer2")
    val widget = Widget(id = 1, name = "Work apps", color = 0, profileId = widgetProfile.id)
    DevDrawerTheme {
        WidgetEditor(
            viewState = WidgetEditorViewState(
                persistedWidget = widget,
                editableWidget = widget.copy(headerColor = WidgetHeaderColor.DARK),
                profiles = listOf(ProfileWithAppCount(widgetProfile, 7), ProfileWithAppCount(WidgetProfile(name = "Default"), 0))
            )
        )
    }
}

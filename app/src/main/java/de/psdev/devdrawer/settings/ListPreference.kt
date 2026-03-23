package de.psdev.devdrawer.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun <T> ListPreference(
    label: String,
    values: Map<T, String>,
    currentValue: T,
    dialogTitle: String = "Select option",
    onClick: (T) -> Unit = {}
) {
    var selectionDialog by remember {
        mutableStateOf(false)
    }
    require(currentValue in values.keys) { "currentValue needs to be a key in values" }
    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 64.dp)
            .fillMaxWidth()
            .clickable { selectionDialog = true }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            text = label
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = requireNotNull(values[currentValue])
        )
        if (selectionDialog) {
            var selection by remember { mutableStateOf(currentValue) }
            AlertDialog(
                onDismissRequest = { selectionDialog = false },
                title = { Text(text = dialogTitle) },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        val list: List<T> = values.keys.toList()
                        items(list) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (item == selection),
                                        onClick = { selection = item },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selection == item,
                                    onClick = null
                                )
                                Text(
                                    text = values[item].orEmpty(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectionDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectionDialog = false
                        onClick(selection)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }
    }
}

@Preview(name = "Light Mode (Enabled)", showSystemUi = true)
@Preview(
    name = "Dark Mode (Enabled)",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview_SelectionPreference_Enabled() {
    DevDrawerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ListPreference(
                label = "Setting 1",
                values = mapOf(
                    SortOrder.LAST_UPDATED to "Last updated",
                    SortOrder.FIRST_INSTALLED to "First installed"
                ),
                currentValue = SortOrder.FIRST_INSTALLED
            )
        }
    }
}

@Preview(name = "Light Mode (Disabled)", showSystemUi = true)
@Preview(
    name = "Dark Mode (Disabled)",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview_SelectionPreference_Disabled() {
    DevDrawerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ListPreference(
                label = "Setting 1",
                values = mapOf(
                    SortOrder.LAST_UPDATED to "Last updated",
                    SortOrder.FIRST_INSTALLED to "First installed"
                ),
                currentValue = SortOrder.FIRST_INSTALLED
            )
        }
    }
}
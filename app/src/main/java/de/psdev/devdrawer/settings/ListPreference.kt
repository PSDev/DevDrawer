package de.psdev.devdrawer.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
            .padding(8.dp)
            .clickable { selectionDialog = true }
    ) {
        Text(style = MaterialTheme.typography.body1, color = MaterialTheme.colors.primary, text = label)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.primaryVariant, text = requireNotNull(values[currentValue]))
        }
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
                                        onClick = { selection = item }
                                    )
                                    .padding(8.dp)

                            ) {
                                RadioButton(
                                    selected = selection == item,
                                    onClick = { selection = item }
                                )
                                Text(
                                    text = values[item].orEmpty(),
                                    style = MaterialTheme.typography.body1.merge(),
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
@Preview(name = "Dark Mode (Enabled)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
@Preview(name = "Dark Mode (Disabled)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
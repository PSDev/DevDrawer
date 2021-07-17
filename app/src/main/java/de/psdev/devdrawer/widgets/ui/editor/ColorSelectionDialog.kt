package de.psdev.devdrawer.widgets.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun ColorSelectionDialog(
    initialColor: Int,
    onColorSelectionChanged: (Int) -> Unit = {},
    onColorSelected: (Int) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Select color")
        },
        text = {
            ColorGrid(
                initialColor = initialColor,
                onColorClicked = {
                    selectedColor = it
                    onColorSelectionChanged(it)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onColorSelected(selectedColor)
            }) {
                Text(stringResource(id = R.string.apply))
            }
        }
    )
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_ColorSelectionDialog() {
    DevDrawerTheme {
        Column {
            ColorSelectionDialog(
                initialColor = android.graphics.Color.BLACK
            )
        }
    }
}
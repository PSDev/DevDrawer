package de.psdev.devdrawer.profiles

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.psdev.devdrawer.R

@Composable
fun WidgetInUseErrorAlertDialog(
    state: DeleteDialogState.InUseError,
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Error")
        },
        text = {
            Text(text = "The profile ${state.widgetProfile.name} is used by: \n" + state.widgets.joinToString("\n") { it.name })
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(stringResource(id = R.string.close))
            }
        }
    )
}
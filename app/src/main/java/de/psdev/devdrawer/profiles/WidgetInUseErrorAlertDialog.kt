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
            Text(text = stringResource(id = R.string.error_profile_in_use))
        },
        text = {
            Text(
                text = stringResource(
                    id = R.string.profile_used_by_widgets,
                    state.widgetProfile.name,
                    state.widgets.joinToString("\n") { it.name }
                )
            )
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
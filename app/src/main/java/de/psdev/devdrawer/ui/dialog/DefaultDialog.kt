package de.psdev.devdrawer.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DefaultDialog(
    onDismissRequest: () -> Unit,
    titleContent: @Composable ColumnScope.() -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp)
            ) {
                Column {
                    titleContent()
                }
                Spacer(modifier = Modifier.size(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false)
                ) {
                    content()
                }
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    bottomContent()
                }
            }
        }
    }

}
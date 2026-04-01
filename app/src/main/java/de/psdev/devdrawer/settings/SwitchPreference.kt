package de.psdev.devdrawer.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun SwitchPreference(
    text: String,
    enabled: Boolean,
    summary: String? = null,
    onChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                text = text
            )
            if (summary != null) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = summary
                )
            }
        }
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}

@Preview(name = "Light Mode (Enabled)")
@Preview(name = "Dark Mode (Enabled)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_SwitchPreference_Enabled() {
    DevDrawerTheme {
        SwitchPreference(text = "Test", enabled = true, summary = "This is a summary")
    }
}

@Preview(name = "Light Mode (Disabled)")
@Preview(name = "Dark Mode (Disabled)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_SwitchPreference_Disabled() {
    DevDrawerTheme {
        SwitchPreference(text = "Test", enabled = false)
    }
}
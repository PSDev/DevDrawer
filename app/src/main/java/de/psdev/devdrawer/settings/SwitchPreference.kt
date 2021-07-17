package de.psdev.devdrawer.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
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
    onChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 64.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(modifier = Modifier.weight(1f), color = MaterialTheme.colors.primary, text = text)
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}

@Preview(name = "Light Mode (Enabled)")
@Preview(name = "Dark Mode (Enabled)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_SwitchPreference_Enabled() {
    DevDrawerTheme {
        SwitchPreference(text = "Test", enabled = true)
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
package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun AppInfoItem(
    appInfo: AppInfo,
    onAppClicked: (AppInfo) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppClicked(appInfo) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember(appInfo.appIcon) {
            appInfo.appIcon.toBitmap().asImageBitmap()
        }
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = appInfo.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${appInfo.packageName} (${appInfo.versionName} - ${appInfo.versionCode})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun Preview_AppInfoItem() {
    val resources = LocalResources.current
    val bitmap = createBitmap(1, 1)
    DevDrawerTheme {
        Surface {
            AppInfoItem(
                appInfo = AppInfo(
                    name = "Example App",
                    packageName = "com.example.app",
                    appIcon = bitmap.toDrawable(resources),
                    firstInstallTime = 0,
                    lastUpdateTime = 0,
                    signatureHashSha256 = "",
                    versionName = "1.0.0",
                    versionCode = 123
                )
            )
        }
    }
}

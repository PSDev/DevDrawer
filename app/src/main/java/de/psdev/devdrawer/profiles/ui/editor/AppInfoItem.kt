package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import de.psdev.devdrawer.R
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
            .defaultMinSize(minHeight = 48.dp)
            .clickable {
                onAppClicked(appInfo)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp),
            bitmap = appInfo.appIcon.toBitmap().asImageBitmap(),
            contentDescription = "App icon"
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(modifier = Modifier.weight(1f), text = appInfo.name, style = MaterialTheme.typography.body1)
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_AppInfoItem() {
    val context = LocalContext.current
    val resources = context.resources
    DevDrawerTheme {
        Surface {
            AppInfoItem(
                appInfo = AppInfo(
                    name = "Test  app",
                    packageName = "Test package",
                    appIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_foreground, context.theme)!!,
                    firstInstallTime = System.currentTimeMillis(),
                    lastUpdateTime = System.currentTimeMillis(),
                    signatureHashSha256 = "1234"
                )
            )
        }
    }
}

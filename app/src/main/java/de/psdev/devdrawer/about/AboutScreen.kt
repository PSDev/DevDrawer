package de.psdev.devdrawer.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import de.psdev.devdrawer.BuildConfig
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.AppIcon

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val libraries by produceLibraries(R.raw.aboutlibraries)
    LibrariesContainer(
        libraries = libraries,
        modifier = modifier.fillMaxSize(),
        header = {
            item {
                AboutHeader()
            }
        },
    )
}

@Composable
internal fun AboutHeader() {
    val context = LocalContext.current
    val appIcon = remember(context) { context.applicationInfo.loadIcon(context.packageManager) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppIcon(icon = appIcon, size = 96.dp)
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = { openPlayStore(context) }) {
                Text(stringResource(R.string.about_play_store))
            }
            OutlinedButton(onClick = { openFirst(context, CustomTabsIntent.Builder().build().intent.setData(SOURCE_URL.toUri())) }) {
                Text(stringResource(R.string.about_github))
            }
        }
    }
}

private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=de.psdev.devdrawer"
private const val SOURCE_URL = "https://github.com/PSDev/DevDrawer"

/** The Play Store app if installed, otherwise its web page in a browser. */
private fun openPlayStore(context: Context) = openFirst(
    context,
    Intent(Intent.ACTION_VIEW, PLAY_STORE_URL.toUri()).setPackage("com.android.vending"),
    Intent(Intent.ACTION_VIEW, PLAY_STORE_URL.toUri())
)

/** Starts the first intent something can handle, or says the device can't. */
private fun openFirst(context: Context, vararg intents: Intent) {
    for (intent in intents) {
        try {
            context.startActivity(intent)
            return
        } catch (e: ActivityNotFoundException) {
            // Try the next one.
        }
    }
    Toast.makeText(context, R.string.action_unavailable, Toast.LENGTH_LONG).show()
}

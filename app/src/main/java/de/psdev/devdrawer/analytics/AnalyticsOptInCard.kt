package de.psdev.devdrawer.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import de.psdev.devdrawer.R
import de.psdev.devdrawer.WidgetListRoute
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.utils.DefaultPreviews

/** The opt-in waits on the Widgets tab instead of interrupting, so it never covers a widget's setup. */
fun showsAnalyticsOptIn(needsOptIn: Boolean, currentRoute: NavKey): Boolean =
    needsOptIn && currentRoute == WidgetListRoute

@Composable
fun AnalyticsOptInCard(
    onOptIn: () -> Unit,
    onOptOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.analytics_opt_in_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.analytics_opt_in_text), style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                TextButton(onClick = onOptOut) { Text(stringResource(R.string.analytics_opt_out)) }
                Button(onClick = onOptIn) { Text(stringResource(R.string.analytics_opt_in)) }
            }
        }
    }
}

@DefaultPreviews
@Composable
private fun Preview_AnalyticsOptInCard() {
    DevDrawerTheme { AnalyticsOptInCard(onOptIn = {}, onOptOut = {}) }
}

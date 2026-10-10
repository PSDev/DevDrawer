package de.psdev.devdrawer.widgets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.widgets.ui.list.WidgetSummary
import de.psdev.devdrawer.widgets.ui.widgetHeaderColors

/**
 * A placed widget: its header colour, name, profile and app count. A widget that matches no apps says so;
 * tapping it then opens setup instead of the editor.
 */
@Composable
fun WidgetCard(
    summary: WidgetSummary,
    onWidgetClick: (Widget) -> Unit = {},
    onChooseAppsClick: (Widget) -> Unit = {}
) {
    val noApps = summary.appCount == 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { if (noApps) onChooseAppsClick(summary.widget) else onWidgetClick(summary.widget) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderSwatchBox(summary.widget.headerColor)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = summary.widget.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (noApps) {
                        stringResource(R.string.widget_card_no_apps)
                    } else {
                        stringResource(
                            R.string.widget_card_summary,
                            summary.profileName,
                            pluralStringResource(R.plurals.widget_app_count, summary.appCount, summary.appCount)
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (noApps) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeaderSwatchBox(headerColor: WidgetHeaderColor) {
    val colors = widgetHeaderColors(headerColor)
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(colors.container, MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetCard() {
    DevDrawerTheme {
        Column {
            WidgetCard(WidgetSummary(Widget(1, "Work apps", 0, ""), profileName = "Signed like DevDrawer2", appCount = 7))
            WidgetCard(WidgetSummary(Widget(5, "Widget 5", 0, "", headerColor = WidgetHeaderColor.DARK), profileName = "Default", appCount = 0))
        }
    }
}

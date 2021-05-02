package de.psdev.devdrawer.widgets.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import de.psdev.devdrawer.widgets.WidgetCard

@Composable
fun WidgetList(
    widgets: List<Widget>,
    onWidgetClick: (Widget) -> Unit = {},
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = contentPadding,
    ) {
        items(widgets, key = { it.id }) { widget ->
            WidgetCard(widget = widget, onWidgetClick = onWidgetClick)
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetList() {
    DevDrawerTheme {
        WidgetList(widgets = testWidgets())
    }
}
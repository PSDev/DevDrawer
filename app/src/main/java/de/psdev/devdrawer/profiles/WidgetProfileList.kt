package de.psdev.devdrawer.profiles

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetProfileList(
    widgetProfiles: List<WidgetProfile>,
    modifier: Modifier = Modifier,
    onWidgetProfileClick: (WidgetProfile) -> Unit = {},
    onWidgetProfileLongClick: (WidgetProfile) -> Unit = {},
    onEditProfile: (WidgetProfile) -> Unit = {},
    onDeleteProfile: (WidgetProfile) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        items(widgetProfiles, key = { it.id }) { widgetProfile ->
            val dismissState = rememberSwipeToDismissBoxState()

            LaunchedEffect(dismissState.currentValue) {
                when (dismissState.currentValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onEditProfile(widgetProfile)
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        onDeleteProfile(widgetProfile)
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }

                    else -> {}
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val direction = dismissState.dismissDirection
                    val color by animateColorAsState(
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }, label = "dismiss_color"
                    )
                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                    val icon = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                        else -> null
                    }
                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                        label = "dismiss_scale"
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .background(color, MaterialTheme.shapes.medium)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        if (icon != null) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.scale(scale),
                                tint = when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                },
                content = {
                    WidgetProfileCard(
                        widgetProfile = widgetProfile,
                        onWidgetProfileClick = onWidgetProfileClick,
                        onWidgetProfileLongClick = onWidgetProfileLongClick
                    )
                }
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_WidgetProfileList() {
    DevDrawerTheme {
        WidgetProfileList(
            listOf(
                WidgetProfile(name = "Profile 1"),
                WidgetProfile(name = "Profile 2")
            )
        )
    }
}
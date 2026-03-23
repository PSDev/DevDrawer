package de.psdev.devdrawer

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

typealias AppBarActions = @Composable RowScope.() -> Unit
typealias AppBarActionsProvider = (AppBarActions?) -> Unit

@Composable
fun ProvideMenu(
    actionsProvider: AppBarActionsProvider,
    updateKey: Any? = null,
    actions: AppBarActions
) {
    LaunchedEffect(key1 = updateKey) {
        actionsProvider(actions)
    }
}

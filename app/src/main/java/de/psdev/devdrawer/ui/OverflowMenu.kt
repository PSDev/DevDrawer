package de.psdev.devdrawer.ui

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import de.psdev.devdrawer.R

interface OverflowMenuScope {
    fun closeMenu()
}

@Composable
fun OverflowMenu(content: @Composable OverflowMenuScope.() -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val scope = remember {
        object : OverflowMenuScope {
            override fun closeMenu() {
                showMenu = false
            }
        }
    }
    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.more),
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        scope.content()
    }
}
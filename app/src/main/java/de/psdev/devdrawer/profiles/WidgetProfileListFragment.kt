package de.psdev.devdrawer.profiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProfileListFragment: BaseFragment() {

    companion object: KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            DevDrawerTheme {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.profiles)
    }

}

@Composable
fun WidgetInUseErrorAlertDialog(
    state: DeleteDialogState.InUseError,
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Error")
        },
        text = {
            Text(text = "The profile ${state.widgetProfile.name} is used by: \n" + state.widgets.joinToString("\n") { it.name })
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(stringResource(id = R.string.close))
            }
        }
    )
}

sealed class DeleteDialogState {
    object Hidden: DeleteDialogState()
    data class Showing(
        val widgetProfile: WidgetProfile
    ): DeleteDialogState()

    data class InUseError(
        val widgetProfile: WidgetProfile,
        val widgets: List<Widget>
    ): DeleteDialogState()
}

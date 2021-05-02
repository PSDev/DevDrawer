package de.psdev.devdrawer.widgets.ui.list

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.receivers.PinWidgetSuccessReceiver
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class WidgetListFragment: BaseFragment() {

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
                WidgetListScreen(
                    navController = findNavController(),
                    onWidgetClick = { widget ->
                        findNavController().navigate(WidgetListFragmentDirections.editWidget(widget.id))
                    },
                    onRequestPinWidgetClick = ::requestAppWidgetPinning
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.widgets)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAppWidgetPinning() {
        val activity = requireActivity()
        val appWidgetManager: AppWidgetManager = activity.getSystemService() ?: return
        val widgetProvider = ComponentName(activity, DDWidgetProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val intent = PinWidgetSuccessReceiver.intent(activity)
            val successCallback = PendingIntent.getBroadcast(
                activity,
                1,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            val bundle = bundleOf()
            appWidgetManager.requestPinAppWidget(widgetProvider, bundle, successCallback)
        }
    }
}
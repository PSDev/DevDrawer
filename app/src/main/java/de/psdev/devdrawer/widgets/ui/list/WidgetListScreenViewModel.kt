package de.psdev.devdrawer.widgets.ui.list

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.receivers.PinWidgetSuccessReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WidgetListScreenViewModel @Inject constructor(
    private val application: Application,
    database: DevDrawerDatabase
) : ViewModel() {

    val state = database.widgetDao().findAllFlow()
        .map { widgets ->
            val appWidgetManager: AppWidgetManager? = application.getSystemService()
            WidgetListScreenState.Loaded(
                widgets = widgets,
                isRequestPinAppWidgetSupported = appWidgetManager?.isRequestPinAppWidgetSupported == true
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetListScreenState.Loading)

    @SuppressLint("InlinedApi")
    fun requestAppWidgetPinning(context: Context) {
        val appWidgetManager: AppWidgetManager = context.getSystemService() ?: return
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val widgetProvider = ComponentName(context, DDWidgetProvider::class.java)
            val successCallback = PendingIntent.getBroadcast(
                context,
                1,
                PinWidgetSuccessReceiver.intent(context),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
            )
            val bundle = bundleOf()
            appWidgetManager.requestPinAppWidget(widgetProvider, bundle, successCallback)
        }
    }
}

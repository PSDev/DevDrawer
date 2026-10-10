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
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
import de.psdev.devdrawer.receivers.PinWidgetSuccessReceiver
import de.psdev.devdrawer.widgets.IWidgetRepository
import de.psdev.devdrawer.appwidget.PackageHashInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WidgetListScreenViewModel @Inject constructor(
    private val application: Application,
    widgetRepository: IWidgetRepository,
    widgetProfileRepository: IWidgetProfileRepository,
    packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService
) : ViewModel() {

    private val installedPackages = MutableStateFlow<List<PackageHashInfo>?>(null)

    init {
        viewModelScope.launch {
            installedPackages.value = appsService.installedPackages(includeSystemApps = true)
        }
    }

    val state = combine(
        widgetRepository.widgetsFlow(),
        widgetProfileRepository.widgetProfilesFlow(),
        packageFilterRepository.allFiltersFlow(),
        installedPackages.filterNotNull()
    ) { widgets, profiles, filters, packages ->
        val profilesById = profiles.associateBy { it.id }
        val filtersByProfile = filters.groupBy { it.profileId }
        val appWidgetManager: AppWidgetManager? = application.getSystemService()
        WidgetListScreenState.Loaded(
            widgets = widgets.map { widget ->
                WidgetSummary(
                    widget = widget,
                    profileName = profilesById[widget.profileId]?.name.orEmpty(),
                    appCount = packages.matching(filtersByProfile[widget.profileId].orEmpty()).size
                )
            },
            isRequestPinAppWidgetSupported = appWidgetManager?.isRequestPinAppWidgetSupported == true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetListScreenState.Loading)

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

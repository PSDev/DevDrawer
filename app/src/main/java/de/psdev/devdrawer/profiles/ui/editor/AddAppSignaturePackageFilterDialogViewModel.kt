package de.psdev.devdrawer.profiles.ui.editor

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.AppsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class AddAppSignaturePackageFilterDialogViewModel @Inject constructor(
    private val application: Application,
    private val appsService: AppsService
): ViewModel() {

    fun availableApps(currentFilters: List<PackageFilter>) = flow<ViewState> {
        val availableApps = appsService.getInstalledPackages()
            .filter { currentFilters.none { packageFilter -> packageFilter.matches(it) } }
            .mapNotNull { it.toAppInfo(application) }
            .sortedBy { it.name }
        emit(ViewState.Loaded(availableApps))
    }.flowOn(Dispatchers.IO)

    sealed class ViewState {
        object Loading: ViewState() {
            override fun toString(): String = javaClass.simpleName
        }

        data class Loaded(val data: List<AppInfo>): ViewState()
        data class Error(val message: String): ViewState()
    }

}
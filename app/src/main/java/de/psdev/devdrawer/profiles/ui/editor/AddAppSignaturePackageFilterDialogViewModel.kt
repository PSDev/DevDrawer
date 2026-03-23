package de.psdev.devdrawer.profiles.ui.editor

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.apps.AppsService
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.database.PackageFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddAppSignaturePackageFilterDialogViewModel @Inject constructor(
    private val application: Application,
    private val appsService: AppsService
) : ViewModel() {

    val showSystemApps = MutableStateFlow<Boolean>(false)

    fun availableApps(currentFilters: List<PackageFilter>) =
        showSystemApps.transformLatest<Boolean, ViewState> { showSystemApps ->
            emit(ViewState.Loading)
            val availableApps = appsService.getInstalledPackages(showSystemApps)
                .filter { currentFilters.none { packageFilter -> packageFilter.matches(it) } }
                .mapNotNull { it.toAppInfo(application) }
                .sortedBy { it.name }
            emit(ViewState.Loaded(availableApps, showSystemApps))
        }.flowOn(Dispatchers.IO)

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val data: List<AppInfo>,
            val showSystemApps: Boolean
        ) : ViewState()

        data class Error(val message: String) : ViewState()
    }

}
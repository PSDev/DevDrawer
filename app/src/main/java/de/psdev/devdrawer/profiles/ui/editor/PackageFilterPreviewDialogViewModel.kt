package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.apps.AppsService
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class PackageFilterPreviewDialogViewModel @Inject constructor(
    private val appsService: AppsService
) : ViewModel() {

    fun load(packageFilter: PackageFilter) = flow {
        try {
            val appsForPackageFilter = appsService.getAppsForPackageFilter(packageFilter)
            emit(ViewState.Loaded(appsForPackageFilter))
        } catch (e: Exception) {
            emit(ViewState.Error(e.message.orEmpty()))
        }
    }

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val data: List<AppInfo>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

}
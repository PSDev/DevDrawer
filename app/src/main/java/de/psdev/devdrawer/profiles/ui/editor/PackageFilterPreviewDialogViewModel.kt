package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.AppsService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class PackageFilterPreviewDialogViewModel @Inject constructor(
    private val appsService: AppsService
): ViewModel() {

    fun load(packageFilter: PackageFilter) = flow {
        try {
            val appsForPackageFilter = appsService.getAppsForPackageFilter(packageFilter)
            emit(ViewState.Loaded(appsForPackageFilter))
        } catch (e: Exception) {
            emit(ViewState.Error(e.message.orEmpty()))
        }
    }

    sealed class ViewState {
        object Loading: ViewState() {
            override fun toString(): String = javaClass.simpleName
        }

        data class Loaded(val data: List<AppInfo>): ViewState()
        data class Error(val message: String): ViewState()
    }

}
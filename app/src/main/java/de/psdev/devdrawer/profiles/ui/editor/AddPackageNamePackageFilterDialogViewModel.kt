package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.AppsService
import kotlinx.coroutines.flow.flow
import java.text.Collator
import javax.inject.Inject

@HiltViewModel
class AddPackageNamePackageFilterDialogViewModel @Inject constructor(
    private val appsService: AppsService
): ViewModel() {

    fun availablePackageFilters(currentFilters: List<PackageFilter>) = flow<ViewState> {
        val packageNameFilters = appsService.getInstalledPackages()
            .filter { currentFilters.none { packageFilter -> packageFilter.matches(it) } }
            .map { it.packageName }
            .splitIntoFilters()
        emit(ViewState.Loaded(packageNameFilters))
    }

    private fun List<String>.splitIntoFilters(): List<String> {
        val appSet = mutableSetOf<String>()
        forEach { packageName ->
            var tempPackageName = packageName
            appSet.add(tempPackageName)
            while (tempPackageName.isNotEmpty()) {
                val lastIndex = tempPackageName.lastIndexOf(".")
                if (lastIndex > 0) {
                    tempPackageName = tempPackageName.substring(0, lastIndex)
                    appSet.add("$tempPackageName.*")
                } else {
                    tempPackageName = ""
                }
            }
        }
        return appSet.toList().sortedWith(Collator.getInstance())
    }

    sealed class ViewState {
        object Loading: ViewState() {
            override fun toString(): String = javaClass.simpleName
        }

        data class Loaded(val data: List<String>): ViewState()
        data class Error(val message: String): ViewState()
    }

}
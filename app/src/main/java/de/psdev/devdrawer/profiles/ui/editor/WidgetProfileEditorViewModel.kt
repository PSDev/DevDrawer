package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.PackageFilterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetProfileEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val database: DevDrawerDatabase,
    private val packageFilterRepository: PackageFilterRepository
): ViewModel() {

    private val widgetProfileId: String = savedStateHandle.get("profileId")!!
    private val widgetNameState: MutableStateFlow<String?> = MutableStateFlow(null)

    val state = combine(
        database.widgetProfileDao().widgetProfileWithIdObservable(widgetProfileId),
        database.packageFilterDao().findAllByProfileFlow(widgetProfileId).distinctUntilChanged(),
        widgetNameState
    ) { widgetProfile, packageFilters, name ->
        WidgetProfileEditorViewState(
            widgetProfile = widgetProfile,
            widgetName = name ?: widgetProfile.name,
            packageFilters = packageFilters
        )
    }

    fun onNameChanged(newName: String) {
        widgetNameState.value = newName
    }

    fun saveChanges(viewState: WidgetProfileEditorViewState) {
        val widgetProfile = viewState.widgetProfile ?: return
        viewModelScope.launch {
            database.widgetProfileDao().update(
                widgetProfile.copy(
                    name = viewState.widgetName ?: widgetProfile.name
                )
            )
        }
    }

    suspend fun addPackageFilter(packageFilter: PackageFilter) {
        packageFilterRepository.save(packageFilter)
    }

    suspend fun deleteFilter(packageFilter: PackageFilter) {
        packageFilterRepository.delete(packageFilter)
    }

}
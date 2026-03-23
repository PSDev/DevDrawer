package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WidgetProfileEditorViewModel.Factory::class)
class WidgetProfileEditorViewModel @AssistedInject constructor(
    @Assisted private val profileId: String,
    private val database: DevDrawerDatabase,
    private val packageFilterRepository: IPackageFilterRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): WidgetProfileEditorViewModel
    }

    private val widgetNameState: MutableStateFlow<String?> = MutableStateFlow(null)
    private val packageFiltersState: MutableStateFlow<List<PackageFilter>?> = MutableStateFlow(null)

    private val dbFiltersFlow = database.packageFilterDao().findAllByProfileFlow(profileId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state = combine(
        database.widgetProfileDao().widgetProfileWithIdObservable(profileId),
        dbFiltersFlow,
        widgetNameState,
        packageFiltersState
    ) { widgetProfile, dbPackageFilters, name, inMemoryFilters ->
        val currentFilters = inMemoryFilters ?: dbPackageFilters
        val currentName = name ?: widgetProfile?.name.orEmpty()

        val nameChanged = name != null && name != widgetProfile?.name
        // Use a set-based comparison to avoid issues with order or duplicate objects with same content
        val filtersChanged = inMemoryFilters != null && inMemoryFilters.toSet() != dbPackageFilters.toSet()

        WidgetProfileEditorViewState(
            widgetProfile = widgetProfile,
            widgetName = currentName,
            packageFilters = currentFilters,
            isDirty = nameChanged || filtersChanged
        )
    }

    fun onNameChanged(name: String) {
        widgetNameState.value = name
    }

    fun saveChanges(viewState: WidgetProfileEditorViewState) {
        viewModelScope.launch {
            val widgetProfile = viewState.widgetProfile ?: return@launch
            val newName = viewState.widgetName ?: return@launch
            packageFilterRepository.saveProfile(widgetProfile.copy(name = newName), viewState.packageFilters)
            // Reset local state after save
            clearLocalChanges()
        }
    }

    fun addPackageFilter(packageFilter: PackageFilter) {
        viewModelScope.launch {
            val dbFilters = dbFiltersFlow.value
            val currentFilters = packageFiltersState.value ?: dbFilters
            val newFilters = currentFilters + packageFilter
            packageFiltersState.value = if (newFilters.toSet() == dbFilters.toSet()) null else newFilters
        }
    }

    fun deleteFilter(packageFilter: PackageFilter) {
        viewModelScope.launch {
            val dbFilters = dbFiltersFlow.value
            val currentFilters = packageFiltersState.value ?: dbFilters
            val newFilters = currentFilters.filter { it.id != packageFilter.id }
            packageFiltersState.value = if (newFilters.toSet() == dbFilters.toSet()) null else newFilters
        }
    }

    fun clearLocalChanges() {
        widgetNameState.value = null
        packageFiltersState.value = null
    }
}
